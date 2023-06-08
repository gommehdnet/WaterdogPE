/*
 * Copyright 2023 WaterdogTEAM
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

package dev.waterdog.waterdogpe.network.protocol.codec;

import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.packet.EmotePacket;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;
import com.nukkitx.protocol.bedrock.v388.serializer.EmoteSerializer_v388;
import com.nukkitx.protocol.bedrock.v589.BedrockPacketHelper_v589;
import com.nukkitx.protocol.bedrock.v589.serializer.StartGameSerializer_v589;
import dev.waterdog.waterdogpe.network.protocol.ProtocolVersion;

/**
 * @author Kaooot
 * @version 1.0
 */
public class BedrockCodec589 extends BedrockCodec582 {

    @Override
    public ProtocolVersion getProtocol() {
        return ProtocolVersion.MINECRAFT_PE_1_20_0;
    }

    @Override
    public void buildCodec(BedrockPacketCodec.Builder builder) {
        super.buildCodec(builder);
        builder.helper(BedrockPacketHelper_v589.INSTANCE);

        builder.deregisterPacket(StartGamePacket.class);
        builder.registerPacket(StartGamePacket.class, StartGameSerializer_v589.INSTANCE, 11);

        builder.deregisterPacket(EmotePacket.class);
        builder.registerPacket(EmotePacket.class, EmoteSerializer_v388.INSTANCE, 138);
    }
}