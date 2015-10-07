//package org.salmon.remoting.remoting;
//
//import static org.junit.Assert.assertTrue;
//import io.netty.channel.ChannelHandlerContext;
//
//import java.util.concurrent.Executors; 
//
//import org.junit.Test;
//import org.salmon.remoting.CommandCustomHeader;
//import org.salmon.remoting.InvokeCallback;
//import org.salmon.remoting.RemotingClient;
//import org.salmon.remoting.RemotingServer; 
//import org.salmon.remoting.exception.RemotingConnectException;
//import org.salmon.remoting.exception.RemotingSendRequestException;
//import org.salmon.remoting.exception.RemotingTimeoutException;
//import org.salmon.remoting.exception.RemotingTooMuchRequestException;
//import org.salmon.remoting.netty.NettyClientConfig;
//import org.salmon.remoting.netty.NettyRemotingClient;
//import org.salmon.remoting.netty.NettyRemotingServer;
//import org.salmon.remoting.netty.NettyRequestProcessor;
//import org.salmon.remoting.netty.NettyServerConfig;
//import org.salmon.remoting.netty.ResponseFuture;
//import org.salmon.remoting.protocol.RemotingCommand;
//
//
//public class NettyRPCTest {
//    public static RemotingClient createRemotingClient() {
//        NettyClientConfig config = new NettyClientConfig();
//        RemotingClient client = new NettyRemotingClient(config);
//        client.start();
//        return client;
//    }
//
//
//    public static RemotingServer createRemotingServer() throws InterruptedException {
//        NettyServerConfig config = new NettyServerConfig();
//        RemotingServer remotingServer = new NettyRemotingServer(config); 
//        remotingServer.registerProcessor(0, new NettyRequestProcessor() {
//            private int i = 0;
//            @Override
//            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
//                System.out.println("processRequest=" + request + " " + (i++));
//                request.setRemark("hello, I am respponse " + ctx.channel().remoteAddress());
//                return request;
//            }
//        }, Executors.newCachedThreadPool());
//        remotingServer.start();
//        return remotingServer;
//    }
//
//
//    @Test
//    public void test_RPC_Sync() throws InterruptedException, RemotingConnectException,
//            RemotingSendRequestException, RemotingTimeoutException {
//        RemotingServer server = createRemotingServer();
//        RemotingClient client = createRemotingClient();
//
//        for (int i = 0; i < 100; i++) {
//            TestRequestHeader requestHeader = new TestRequestHeader();
//            requestHeader.setCount(i);
//            requestHeader.setMessageTitle("HelloMessageTitle");
//            RemotingCommand request = RemotingCommand.createRequestCommand(0, requestHeader);
//            RemotingCommand response = client.invokeSync("localhost:8888", request, 1000 * 3000);
//            System.out.println("invoke result = " + response);
//            assertTrue(response != null);
//        } 
//
//        client.shutdown();
//        server.shutdown();
//        System.out.println("-----------------------------------------------------------------");
//    }
//
//
//    @Test
//    public void test_RPC_Oneway() throws InterruptedException, RemotingConnectException,
//            RemotingTimeoutException, RemotingTooMuchRequestException, RemotingSendRequestException {
//        RemotingServer server = createRemotingServer();
//        RemotingClient client = createRemotingClient();
//
//        for (int i = 0; i < 100; i++) {
//            RemotingCommand request = RemotingCommand.createRequestCommand(0, null);
//            request.setRemark(String.valueOf(i));
//            client.invokeOneway("localhost:8888", request, 1000 * 3);
//        }
//
//        client.shutdown();
//        server.shutdown();
//        System.out.println("-----------------------------------------------------------------");
//    }
//
//
//    @Test
//    public void test_RPC_Async() throws InterruptedException, RemotingConnectException,
//            RemotingTimeoutException, RemotingTooMuchRequestException, RemotingSendRequestException {
//        RemotingServer server = createRemotingServer();
//        RemotingClient client = createRemotingClient();
//
//        for (int i = 0; i < 100; i++) {
//            RemotingCommand request = RemotingCommand.createRequestCommand(0, null);
//            request.setRemark(String.valueOf(i));
//            client.invokeAsync("localhost:8888", request, 1000 * 3, new InvokeCallback() {
//                @Override
//                public void operationComplete(ResponseFuture responseFuture) {
//                    System.out.println(responseFuture.getResponseCommand());
//                }
//            });
//        }
//
//        Thread.sleep(1000 * 3);
//
//        client.shutdown();
//        server.shutdown();
//        System.out.println("-----------------------------------------------------------------");
//    }
//
//
//    @Test
//    public void test_server_call_client() throws InterruptedException, RemotingConnectException,
//            RemotingSendRequestException, RemotingTimeoutException {
//        final RemotingServer server = createRemotingServer();
//        final RemotingClient client = createRemotingClient();
//
//        server.registerProcessor(0, new NettyRequestProcessor() {
//            @Override
//            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
//                try {
//                    return server.invokeSync(ctx.channel(), request, 1000 * 10);
//                }
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                catch (RemotingSendRequestException e) {
//                    e.printStackTrace();
//                }
//                catch (RemotingTimeoutException e) {
//                    e.printStackTrace();
//                }
//
//                return null;
//            }
//        }, Executors.newCachedThreadPool());
//
//        client.registerProcessor(0, new NettyRequestProcessor() {
//            @Override
//            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
//                System.out.println("client receive server request = " + request);
//                request.setRemark("client remark");
//                return request;
//            }
//        }, Executors.newCachedThreadPool());
//
//        for (int i = 0; i < 3; i++) {
//            RemotingCommand request = RemotingCommand.createRequestCommand(0, null);
//            RemotingCommand response = client.invokeSync("localhost:8888", request, 1000 * 3);
//            System.out.println("invoke result = " + response);
//            assertTrue(response != null);
//        }
//
//        client.shutdown();
//        server.shutdown();
//        System.out.println("-----------------------------------------------------------------");
//    }
//
//}
//
//
//class TestRequestHeader implements CommandCustomHeader { 
//    private Integer count;
// 
//    private String messageTitle;
// 
//
//
//    public Integer getCount() {
//        return count;
//    }
//
//
//    public void setCount(Integer count) {
//        this.count = count;
//    }
//
//
//    public String getMessageTitle() {
//        return messageTitle;
//    }
//
//
//    public void setMessageTitle(String messageTitle) {
//        this.messageTitle = messageTitle;
//    }
//}
//
//
//class TestResponseHeader implements CommandCustomHeader { 
//    private Integer count;
// 
//    private String messageTitle; 
//
//
//    public Integer getCount() {
//        return count;
//    }
//
//
//    public void setCount(Integer count) {
//        this.count = count;
//    }
//
//
//    public String getMessageTitle() {
//        return messageTitle;
//    }
//
//
//    public void setMessageTitle(String messageTitle) {
//        this.messageTitle = messageTitle;
//    }
//}
