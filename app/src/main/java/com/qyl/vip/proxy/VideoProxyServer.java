package com.qyl.vip.proxy;

import android.os.SystemClock;
import android.util.Log;

import com.qyl.vip.proxy.base.IProxyProcessor;
import com.qyl.vip.proxy.exception.VideoProxyException;
import com.qyl.vip.proxy.impl.PingSource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.qyl.vip.proxy.impl.PingSource.PING_RESPONSE;
import static com.qyl.vip.proxy.utils.Preconditions.checkAllNotNull;
import static com.qyl.vip.proxy.utils.VideoProxyUtil.LOG_TAG;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class VideoProxyServer {

    public static final String PROXY_HOST = "127.0.0.1";
    public static final String PING_REQUEST = "ping";

    private final Object clientsLock = new Object();
    private final ExecutorService socketProcessor = Executors.newFixedThreadPool(8);
    private final Map<String, VideoProxyClient> clientsMap = new ConcurrentHashMap<>();
    private final ServerSocket serverSocket;
    private final int port;
    private final Thread waitConnectionThread;
    private boolean pinged;


    public VideoProxyServer() {
        try {
            InetAddress inetAddress = InetAddress.getByName(PROXY_HOST);
            this.serverSocket = new ServerSocket(0, 8, inetAddress);
            this.port = serverSocket.getLocalPort();
            CountDownLatch startSignal = new CountDownLatch(1);
            this.waitConnectionThread = new Thread(new WaitRequestsRunnable(startSignal), "HttpProxyWaitThread");
            this.waitConnectionThread.start();
            startSignal.await();
            Log.i(LOG_TAG, "Video Proxy server started. Ping it...");
            makeSureServerWorks();
        } catch (IOException | InterruptedException e) {
            socketProcessor.shutdown();
            throw new IllegalStateException("Error starting local proxy server", e);
        }
    }

    private void makeSureServerWorks() {
        int maxPingAttempts = 3;
        int delay = 500;
        int pingAttempts = 0;
        while (pingAttempts < maxPingAttempts) {
            try {
                Future<Boolean> pingFuture = socketProcessor.submit(new PingCallable());
                this.pinged = pingFuture.get(delay, MILLISECONDS);
                if (this.pinged) {
                    return;
                }
                SystemClock.sleep(delay);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Log.e(LOG_TAG, "Error pinging server [attempt: " + pingAttempts + ", timeout: " + delay + "]. ", e);
            }
            pingAttempts++;
            delay *= 2;
        }

        Log.e(LOG_TAG, "Shutdown server… Error pinging server [attempts: " + pingAttempts + ", max timeout: " + delay / 2 + "]. ");

        shutdown();
    }

    public void registerProcessor(IProxyProcessor processor, String url) {
        checkAllNotNull(processor, url);
        synchronized (clientsLock) {
            try {
                getClient(url).registerProcessor(processor);
            } catch (VideoProxyException e) {
                Log.d(LOG_TAG, "Error registering cache listener", e);
            }
        }
    }

    public String getProxyUrl(String url) {
        if (!pinged) {
            Log.e(LOG_TAG, "Proxy server isn't pinged. proxy doesn't work.");
        }
        return pinged ? appendToProxyUrl(url) : url;
    }

    private String appendToProxyUrl(String url) {
        return String.format("http://%s:%d/%s", PROXY_HOST, port, url);
    }

    private void shutdown() {
        Log.i(LOG_TAG, "Shutdown proxy server");

        shutdownClients();

        waitConnectionThread.interrupt();
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            onError(new VideoProxyException("Error shutting down proxy server", e));
        }
    }

    private void shutdownClients() {
        synchronized (clientsLock) {
            for (VideoProxyClient clients : clientsMap.values()) {
                clients.shutdown();
            }
            clientsMap.clear();
        }
    }

    private VideoProxyClient getClient(String url) throws VideoProxyException {
        synchronized (clientsLock) {
            VideoProxyClient clients = clientsMap.get(url);
            if (clients == null) {
                clients = new VideoProxyClient(url);
                clientsMap.put(url, clients);
            }
            return clients;
        }
    }

    private int getClientsCount() {
        synchronized (clientsLock) {
            int count = 0;
            for (VideoProxyClient client : clientsMap.values()) {
                count += client.getClientsCount();
            }
            return count;
        }
    }

    private void onError(Throwable e) {
        Log.e(LOG_TAG, "VideoProxyServer error", e);

        e.printStackTrace();
    }


    private final class WaitRequestsRunnable implements Runnable {

        private final CountDownLatch startSignal;

        public WaitRequestsRunnable(CountDownLatch startSignal) {
            this.startSignal = startSignal;
        }

        @Override
        public void run() {
            startSignal.countDown();
            Log.i(LOG_TAG, "WaitRequestsRunnable run waitForRequest");
            waitForRequest();
        }

        private void waitForRequest() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    Log.d(LOG_TAG, "Accept new socket " + socket);
                    socketProcessor.submit(new SocketProcessorRunnable(socket));
                }
            } catch (IOException e) {
                onError(new VideoProxyException("Error during waiting connection", e));
            }
        }
    }

    private final class SocketProcessorRunnable implements Runnable {

        private final Socket socket;

        public SocketProcessorRunnable(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            processSocket(socket);
        }

        private void processSocket(Socket socket) {
            Log.e(LOG_TAG, "processSocket " + socket);

            try {
                GetRequest request = GetRequest.read(socket.getInputStream());
                Log.i(LOG_TAG, "Request to  proxy:" + request);
                String url = request.uri;
                VideoProxyClient client = getClient(url);
                client.processRequest(request, socket);
                /*if (PING_REQUEST.equals(url)) {
                    //responseToPing(socket);
                    VideoProxyClient client = getClient(url);
                    client.processRequest(request, socket);
                } else {

                }*/
            } catch (SocketException e) {
                Log.d(LOG_TAG, "Closing socket… Socket is closed by client." + e.getMessage());
            } catch (VideoProxyException | IOException e) {
                e.printStackTrace();

                onError(new VideoProxyException("Error processing request", e));
            } catch (Exception e) {
                e.printStackTrace();

                onError(new VideoProxyException("Error processing request", e));
            } finally {
                releaseSocket(socket);
                Log.d(LOG_TAG, "Opened connections: " + getClientsCount());
            }
        }

        private void releaseSocket(Socket socket) {
            closeSocketInput(socket);
            closeSocketOutput(socket);
            closeSocket(socket);
        }

        private void responseToPing(Socket socket) throws IOException {
            OutputStream out = socket.getOutputStream();
            out.write("HTTP/1.1 200 OK\n\n".getBytes());
            out.write(PING_RESPONSE.getBytes());
        }

        private void closeSocketInput(Socket socket) {
            try {
                if (!socket.isInputShutdown()) {
                    socket.shutdownInput();
                }
            } catch (SocketException e) {
                Log.d(LOG_TAG, "Releasing input stream… Socket is closed by client.");
            } catch (IOException e) {
                onError(new VideoProxyException("Error closing socket input stream", e));
            }
        }

        private void closeSocketOutput(Socket socket) {
            try {
                if (socket.isOutputShutdown()) {
                    socket.shutdownOutput();
                }
            } catch (IOException e) {
                onError(new VideoProxyException("Error closing socket output stream", e));
            }
        }

        private void closeSocket(Socket socket) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                onError(new VideoProxyException("Error closing socket", e));
            }
        }
    }

    private class PingCallable implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            Log.e(LOG_TAG, "PingCallable call");

            return pingServer();
        }

        private boolean pingServer() throws VideoProxyException {
            String pingUrl = appendToProxyUrl(PING_REQUEST);
            PingSource source = new PingSource(pingUrl);
            try {
                byte[] expectedResponse = PING_RESPONSE.getBytes();
                source.open(0);
                byte[] response = new byte[expectedResponse.length];
                source.read(response);
                boolean pingOk = Arrays.equals(expectedResponse, response);
                Log.d(LOG_TAG, "Ping response: `" + new String(response) + "`, pinged? " + pingOk);
                return pingOk;
            } catch (VideoProxyException e) {
                Log.e(LOG_TAG, "Error reading ping response", e);
                return false;
            } finally {
                source.close();
            }
        }
    }
}
