package node.connection.hyperledger.fabric;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.msp.Identities;
import org.hyperledger.fabric.sdk.TransactionInfo;
import org.hyperledger.fabric.sdk.transaction.ProtoUtils;

@Builder
@Getter
@Setter
@ToString
public class FabricHeader {
    private String txId;
    private String channel;
    private String timestamp;
    private int type;
    private String nonce;
    private String creatorMspId;

    public static FabricHeader of(TransactionInfo info) {

        try {
            Common.Payload payload = Common.Payload.parseFrom(info.getEnvelope().getPayload());
            Common.Header header = payload.getHeader();

            Common.SignatureHeader signatureHeader = Common.SignatureHeader.parseFrom(header.getSignatureHeader());
            Identities.SerializedIdentity creator = Identities.SerializedIdentity.parseFrom(signatureHeader.getCreator());

            Common.ChannelHeader channelHeader = Common.ChannelHeader.parseFrom(header.getChannelHeader());
            long timestamp = ProtoUtils.getDateFromTimestamp(channelHeader.getTimestamp()).getTime();

            return FabricHeader.builder()
                    .txId(info.getTransactionID())
                    .channel(channelHeader.getChannelId())
                    .timestamp(timestamp + "")
                    .type(channelHeader.getType())
                    .nonce(Hex.toHexString(signatureHeader.getNonce().toByteArray()))
                    .creatorMspId(creator.getMspid())
                    .build();

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.INVALID_PROTOCOL_BUFFER_ERROR);
        }
    }
}
