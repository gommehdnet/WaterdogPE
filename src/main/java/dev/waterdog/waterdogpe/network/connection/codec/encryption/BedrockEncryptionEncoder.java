/*
 * Copyright 2022 WaterdogTEAM
 * Licensed under the GNU General Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.waterdog.waterdogpe.network.connection.codec.encryption;

import dev.waterdog.waterdogpe.network.connection.codec.BedrockBatchWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class BedrockEncryptionEncoder extends MessageToMessageEncoder<BedrockBatchWrapper> {

    public static final String NAME = "bedrock-encryption-encoder";

    private static final FastThreadLocal<MessageDigest> DIGEST = new FastThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }
    };

    private final AtomicLong packetCounter = new AtomicLong();
    private final SecretKey key;
    private final Cipher cipher;

    @Override
    protected void encode(ChannelHandlerContext ctx, BedrockBatchWrapper msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().ioBuffer(msg.getCompressed().readableBytes() + 8);
        try {
            ByteBuffer trailer = ByteBuffer.wrap(generateTrailer(msg.getCompressed(), this.key, this.packetCounter));
            ByteBuffer inBuffer = msg.getCompressed().nioBuffer();
            ByteBuffer outBuffer = buf.nioBuffer(0, msg.getCompressed().readableBytes() + 8);

            int index = this.cipher.update(inBuffer, outBuffer);
            index += this.cipher.update(trailer, outBuffer);

            buf.writerIndex(index);
            msg.setCompressed(buf.retain());
            out.add(msg.retain());
        } finally {
            buf.release();
        }
    }

    static byte[] generateTrailer(ByteBuf buf, SecretKey key, AtomicLong counter) {
        MessageDigest digest = DIGEST.get();
        ByteBuf counterBuf = ByteBufAllocator.DEFAULT.directBuffer(8);
        try {
            counterBuf.writeLongLE(counter.getAndIncrement());
            ByteBuffer keyBuffer = ByteBuffer.wrap(key.getEncoded());

            digest.update(counterBuf.nioBuffer(0, 8));
            digest.update(buf.nioBuffer(buf.readerIndex(), buf.readableBytes()));
            digest.update(keyBuffer);
            byte[] hash = digest.digest();
            return Arrays.copyOf(hash, 8);
        } finally {
            counterBuf.release();
            digest.reset();
        }
    }
}
