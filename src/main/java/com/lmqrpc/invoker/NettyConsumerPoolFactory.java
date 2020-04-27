package com.lmqrpc.invoker;

import com.google.common.collect.Lists;
import com.lmqrpc.entity.RcResponse;
import com.lmqrpc.entity.ReServiceProvider;
import com.lmqrpc.seriable.ReNettyDecoder;
import com.lmqrpc.seriable.ReNettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class NettyConsumerPoolFactory {

    public static NettyConsumerPoolFactory nettyConsumerPoolFactory=new NettyConsumerPoolFactory();


    private static final ConcurrentHashMap<InetSocketAddress, ArrayBlockingQueue<Channel>> channelpoolMap=new ConcurrentHashMap<InetSocketAddress, ArrayBlockingQueue<Channel>>();

    private static final int connectionsize=1000;

    private List<ReServiceProvider> serviceProviderList=new ArrayList<ReServiceProvider>();


    public static NettyConsumerPoolFactory getSingleton()
    {

        return nettyConsumerPoolFactory;
    }


    public void initChannelPoolFactory(HashMap<String, List<ReServiceProvider>> providerMap) {
        //将服务提供者信息存入serviceMetaDataList列表
        Collection<List<ReServiceProvider>> collectionServiceMetaDataList = providerMap.values();
        for (List<ReServiceProvider> serviceMetaDataModels : collectionServiceMetaDataList) {
            if (CollectionUtils.isEmpty(serviceMetaDataModels)) {
                continue;
            }
            serviceProviderList.addAll(serviceMetaDataModels);
        }

        //获取服务提供者地址列表
        Set<InetSocketAddress> socketAddressSet = new HashSet();
        for (ReServiceProvider serviceMetaData : serviceProviderList) {
            String serviceIp = serviceMetaData.getProviderIp();
            int servicePort = serviceMetaData.getProviderPort();

            InetSocketAddress socketAddress = new InetSocketAddress(serviceIp, servicePort);
            socketAddressSet.add(socketAddress);
        }

        //根据服务提供者地址列表初始化Channel阻塞队列,并以地址为Key,地址对应的Channel阻塞队列为value,存入channelPoolMap
        for (InetSocketAddress socketAddress : socketAddressSet) {
            try {
                int realChannelConnectSize = 0;
                while (realChannelConnectSize < connectionsize) {
                    Channel channel = null;
                    while (channel == null) {
                        //若channel不存在,则注册新的Netty Channel
                        channel = registerChannel(socketAddress);
                    }
                    //计数器,初始化的时候存入阻塞队列的Netty Channel个数不超过channelConnectSize
                    realChannelConnectSize++;

                    //将新注册的Netty Channel存入阻塞队列channelArrayBlockingQueue
                    // 并将阻塞队列channelArrayBlockingQueue作为value存入channelPoolMap
                    ArrayBlockingQueue<Channel> channelArrayBlockingQueue = channelpoolMap.get(socketAddress);
                    if (channelArrayBlockingQueue == null) {
                        channelArrayBlockingQueue = new ArrayBlockingQueue<Channel>(connectionsize);
                        channelpoolMap.put(socketAddress, channelArrayBlockingQueue);
                    }
                    channelArrayBlockingQueue.offer(channel);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 为服务提供者地址socketAddress注册新的Channel
     *
     * @param socketAddress
     * @return
     */
    public Channel registerChannel(InetSocketAddress socketAddress) {
        try {
            EventLoopGroup group = new NioEventLoopGroup(10);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(socketAddress);

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //注册Netty编码器
                            ch.pipeline().addLast(new ReNettyEncoder());
                            //注册Netty解码器
                            ch.pipeline().addLast(new ReNettyDecoder(RcResponse.class));
                            //注册客户端业务逻辑处理handler
                            ch.pipeline().addLast(new NettyConsumerHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel newChannel = channelFuture.channel();
            final CountDownLatch connectedLatch = new CountDownLatch(1);

            final List<Boolean> isSuccessHolder = Lists.newArrayListWithCapacity(1);
            //监听Channel是否建立成功
            channelFuture.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                    //若Channel建立成功,保存建立成功的标记
                    if (future.isSuccess()) {
                        isSuccessHolder.add(Boolean.TRUE);
                    } else {
                        //若Channel建立失败,保存建立失败的标记
                        future.cause().printStackTrace();
                        isSuccessHolder.add(Boolean.FALSE);
                    }
                    connectedLatch.countDown();
                }
            });

            connectedLatch.await();
            //如果Channel建立成功,返回新建的Channel
            if (isSuccessHolder.get(0)) {
                return newChannel;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress) {
        return channelpoolMap.get(socketAddress);
    }







}
