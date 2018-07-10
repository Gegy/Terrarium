package net.gegy1000.terrarium.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.terrarium.client.ClientProxy;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingState;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TerrariumLoadingStateMessage implements IMessage {
	private LoadingState state;

    public TerrariumLoadingStateMessage() {
    }

    public TerrariumLoadingStateMessage(LoadingState state) {
        this.state = state;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	if (buf.readBoolean()) {
    		this.state = LoadingState.values()[buf.readInt()];
    	}
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	buf.writeBoolean(this.state != null);
    	if (this.state != null) {
    		buf.writeInt(this.state.ordinal());
    	}
    }

    public static class Handler implements IMessageHandler<TerrariumLoadingStateMessage, IMessage> {
        @Override
        public IMessage onMessage(TerrariumLoadingStateMessage message, MessageContext ctx) {
        	ClientProxy.loadingState = message.state;
            return null;
        }
    }
}
