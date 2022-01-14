package com.hjcenry.kcp.listener;

import com.hjcenry.kcp.Ukcp;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;

/**
 * {@link KtucpListener} which allows to explicit only handle a specific type of messages.
 * <p>
 * For example here is an implementation which only handle {@link String} messages.
 *
 * <pre>
 *     public class StringHandler extends
 *             {@link SimpleKtucpListener}&lt;{@link String}&gt; {
 *
 *         {@code @Override}
 *         protected void handleReceive0({@link String} message, {@link Ukcp} ukcp)
 *                 throws {@link Exception} {
 *             System.out.println(message);
 *         }
 *     }
 * </pre>
 * <p>
 * Be aware that depending of the constructor parameters it will release all handled messages by passing them to
 * {@link ReferenceCountUtil#release(Object)}. In this case you may need to use
 * {@link ReferenceCountUtil#retain(Object)} if you pass the object.
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/11 15:26
 **/
public abstract class SimpleKtucpListener<I> implements KtucpListener {

    private final TypeParameterMatcher matcher;

    /**
     * Create a new instance which will try to detect the types to match out of the type parameter of the class.
     */
    protected SimpleKtucpListener() {
        this.matcher = TypeParameterMatcher.find(this, SimpleKtucpListener.class, "I");
    }

    @Override
    public void handleReceive(Object object, Ukcp ukcp) throws Exception {
        if (acceptInboundMessage(object)) {
            @SuppressWarnings("unchecked")
            I msg = (I) object;
            handleReceive0(msg, ukcp);
        }
    }

    /**
     * Is called for each message of type {@link I}.
     *
     * @param cast the message to handle
     * @param ukcp kcp user instance
     * @throws Exception is thrown if an error occurred
     */
    protected abstract void handleReceive0(I cast, Ukcp ukcp) throws Exception;

    /**
     * Returns {@code true} if the given message should be handled.
     */
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return matcher.match(msg);
    }
}
