package com.zqh.midd.netty.gameserver.manager;

import com.zqh.midd.netty.gameserver.codec.ServerDecoder;
import com.zqh.midd.netty.gameserver.codec.ServerEncoder;
import com.zqh.midd.netty.gameserver.handler.ServerHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * User: mengmeng.cheng
 * Date: 4/11/14
 * Time: 10:28 AM
 * Email: chengmengmeng@gmail.com
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", new ServerDecoder());
        pipeline.addLast("encoder", new ServerEncoder());

//        pipeline.addLast("decoder", new StringDecoder());
//        pipeline.addLast("encoder", new StringEncoder());

        pipeline.addLast("handler", new ServerHandler());
    }
}
