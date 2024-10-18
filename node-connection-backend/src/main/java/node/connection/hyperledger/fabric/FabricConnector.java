package node.connection.hyperledger.fabric;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class FabricConnector {

    private HFClient hfClient;
    private Channel curChannel;
    private ChaincodeID chaincodeID;
    private final ChannelManager channelManager;

    public FabricConnector(Client client) {
        hfClient = createHFClient(client);
        channelManager = new ChannelManager();
    }

    public FabricConnector(NetworkConfig networkConfig, Client client) {
        hfClient = createHFClient(client);
        channelManager = new ChannelManager();
        connectToChannel(networkConfig);
    }

    public void resetClient(Client client) {
        hfClient = createHFClient(client);
        channelManager.invalidateAll();
        curChannel = null;
    }

    public Channel connectToChannel(NetworkConfig networkConfig) {
        return connectToChannel(networkConfig, networkConfig.getChannelName());
    }

    public Channel connectToChannel(NetworkConfig networkConfig, String channelName) {
        Channel channel = channelManager.get(channelName);
        if (channel == null) {
            channel = hfClient.getChannel(channelName);
            if (channel == null) {
                channel = createChannel(networkConfig, channelName);
            }
            channelManager.put(channel);
        }
        curChannel = channel;
        return channel;
    }

    private HFClient createHFClient(Client client) {
        try {
            CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
            HFClient instance = HFClient.createNewInstance();
            instance.setCryptoSuite(cryptoSuite);
            instance.setUserContext(client);
            return instance;
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException |
                CryptoException | InvalidArgumentException |
                NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CLIENT_CREATION_ERROR);
        }
    }

    private Channel createChannel(NetworkConfig networkConfig) {
        return createChannel(networkConfig, networkConfig.getChannelName());
    }

    private Channel createChannel(NetworkConfig networkConfig, String channelName) {
        try {
            Channel channel = hfClient.newChannel(channelName);
            for (Node peer : networkConfig.getPeers()) {
                channel.addPeer(hfClient.newPeer(peer.getName(), peer.getUrl(), peer.getProperties()));
            }

            channel.addOrderer(
                    hfClient.newOrderer(
                            networkConfig.getOrderer().getName(),
                            networkConfig.getOrderer().getUrl(),
                            networkConfig.getOrderer().getProperties()
                    )
            );

            if (networkConfig.getNode() != null) {
                channel.addPeer(
                        hfClient.newPeer(
                                networkConfig.getNode().getName(),
                                networkConfig.getNode().getUrl()
                        )
                );
            }

            channel.initialize();
            log.info("CHANNEL [{}] INITIALIZED : [{}]", channel.getName(), channel.isInitialized());

            return channel;
        } catch (InvalidArgumentException | TransactionException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CHANNEL_CREATION_ERROR);
        }
    }

    public void setChaincode(String name, String version) {
        chaincodeID = ChaincodeID.newBuilder()
                .setName(name)
                .setVersion(version)
                .build();
    }

    public void setChaincodeID(ChaincodeID chaincodeID) {
        if (chaincodeID == null) {
            throw new IllegalArgumentException("chaincodeID is null");
        }
        this.chaincodeID = chaincodeID;
    }

    private void checkForInvokeAndQuery() {
        checkChannel();
        checkChaincodeID();
    }

    private void checkChaincodeID() {
        if (chaincodeID == null) {
            throw new IllegalArgumentException("chaincodeID is null");
        }
    }

    private void checkChannel() {
        if (curChannel == null) {
            throw new IllegalArgumentException("channel is null");
        }
    }

    public FabricProposalResponse invoke(String fcn, List<String> args) {
        checkForInvokeAndQuery();

        try {
            TransactionProposalRequest request = hfClient.newTransactionProposalRequest();
            request.setChaincodeID(chaincodeID);
            request.setFcn(fcn);
            request.setArgs(new ArrayList<>(args));
            request.setProposalWaitTime(180000);

            Collection<ProposalResponse> responses = curChannel.sendTransactionProposal(request);

            FabricProposalResponse r = proposalResponseInterceptor(responses);
            if (r.getSuccess()) {
                curChannel.sendTransaction(responses).get();
            }
            return r;

        } catch (InvalidArgumentException | ProposalException | ExecutionException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_TRANSACTION_ERROR);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_TRANSACTION_ERROR);
        }

    }

    public FabricProposalResponse query(String fcn, List<String> args) {
        checkForInvokeAndQuery();

        QueryByChaincodeRequest request = hfClient.newQueryProposalRequest();
        request.setChaincodeID(chaincodeID);
        request.setFcn(fcn);
        request.setArgs(new ArrayList<>(args));
        request.setProposalWaitTime(180000);

        try {
            Collection<ProposalResponse> responses = curChannel.queryByChaincode(request);
            return proposalResponseInterceptor(responses);
        } catch (InvalidArgumentException | ProposalException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_TRANSACTION_ERROR);
        }
    }

    public FabricProposalResponse installChaincode(String sourcePath, String metaPath) {
        checkForInvokeAndQuery();
        if (chaincodeID.getPath() == null) {
            throw new IllegalArgumentException("chaincodeID.path is null");
        }

        try {
            InstallProposalRequest installProposalRequest = hfClient.newInstallProposalRequest();
            installProposalRequest.setChaincodeID(chaincodeID);
            installProposalRequest.setChaincodeSourceLocation(new File(sourcePath));
            if (metaPath != null) {
                installProposalRequest.setChaincodeMetaInfLocation(new File(metaPath));
            }
            installProposalRequest.setProposalWaitTime(90000);

            Collection<ProposalResponse> responses = hfClient.sendInstallProposal(installProposalRequest, curChannel.getPeers());
            return proposalResponseInterceptor(responses);

        } catch (InvalidArgumentException | ProposalException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CHAINCODE_INSTALLATION_ERROR);
        }
    }

    public FabricProposalResponse instantiateChaincode() {
        return instantiateChaincode(null);
    }

    public FabricProposalResponse instantiateChaincode(String policyPath) {
        checkForInvokeAndQuery();
        if (chaincodeID.getPath() == null) {
            throw new IllegalArgumentException("chaincodeID.path is null");
        }

        try {
            InstantiateProposalRequest instantiateProposalRequest = hfClient.newInstantiationProposalRequest();
            instantiateProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            instantiateProposalRequest.setChaincodeID(chaincodeID);
            instantiateProposalRequest.setProposalWaitTime(90000);
            instantiateProposalRequest.setFcn("init");
            instantiateProposalRequest.setArgs("");

            if (policyPath != null) {
                try {
                    ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
                    chaincodeEndorsementPolicy.fromYamlFile(new File(policyPath));
                    instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
                } catch (IOException | ChaincodeEndorsementPolicyParseException e) {
                    e.printStackTrace();
                    throw new ServerException(ExceptionStatus.FABRIC_CHAINCODE_INSTANTIATION_ERROR);
                }
            }

            Collection<ProposalResponse> responses = curChannel.sendInstantiationProposal(instantiateProposalRequest, curChannel.getPeers());
            FabricProposalResponse r = proposalResponseInterceptor(responses);
            if (r.getSuccess()) {
                curChannel.sendTransaction(responses).get();
            }
            return r;

        } catch (InvalidArgumentException | ProposalException | ExecutionException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CHAINCODE_INSTANTIATION_ERROR);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CHAINCODE_INSTANTIATION_ERROR);
        }
    }

    public FabricProposalResponse upgradeChaincode() {
        checkForInvokeAndQuery();
        if (chaincodeID.getPath() == null) {
            throw new IllegalArgumentException("chaincodeID.path is null");
        }

        try {
            UpgradeProposalRequest upgradeProposalRequest = hfClient.newUpgradeProposalRequest();
            upgradeProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            upgradeProposalRequest.setFcn("init");
            upgradeProposalRequest.setArgs("");
            upgradeProposalRequest.setChaincodeID(chaincodeID);
            upgradeProposalRequest.setProposalWaitTime(90000);

            Collection<ProposalResponse> responses = curChannel.sendUpgradeProposal(upgradeProposalRequest, curChannel.getPeers());
            return proposalResponseInterceptor(responses);

        } catch (InvalidArgumentException | ProposalException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CHAINCODE_UPGRADE_ERROR);
        }
    }

    public BlockchainInfo[] queryBlockchainInfo() {
        AtomicLong atomicHeight = new AtomicLong(Long.MAX_VALUE);
        BlockchainInfo[] blockchainInfo = new BlockchainInfo[1];
        curChannel.getPeers().forEach(peer -> {
            try {
                BlockchainInfo channelInfo;
                channelInfo = curChannel.queryBlockchainInfo(peer, hfClient.getUserContext());
                final long height = channelInfo.getHeight();
                if (height < atomicHeight.longValue()) {
                    atomicHeight.set(height);
                    blockchainInfo[0] = channelInfo;
                }
            } catch (ProposalException | InvalidArgumentException e) {
                e.printStackTrace();
                throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
            }
        });
        return blockchainInfo;
    }

    public long getBlockHeight() {
        BlockchainInfo[] blockchainInfo = queryBlockchainInfo();
        if (blockchainInfo[0] == null) {
            return 0L;
        }
        return blockchainInfo[0].getHeight();
    }

    public BlockInfo queryBlockByNumber(long height) {
        checkChannel();
        try {
            return curChannel.queryBlockByNumber(height);
        } catch (InvalidArgumentException | ProposalException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }
    }

    public FabricTransactionInfo getTransactionInfo(String txId) {
        checkChannel();
        try {
            TransactionInfo info = curChannel.queryTransactionByID(txId);
            return FabricTransactionInfo.of(info);
        } catch (ProposalException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }
    }

    private FabricProposalResponse proposalResponseInterceptor(Collection<ProposalResponse> responses) {
        if (responses.isEmpty()) {
            throw new IllegalArgumentException("responses.size is 0");
        }

        ProposalResponse r = null;
        for (ProposalResponse response : responses) {
            r = response;
            try {
                log.info("chaincode propsal - chaincode: name=[{}], version=[{}], status=[{}], message[{}]",
                        response.getChaincodeID().getName(), response.getChaincodeID().getVersion(),
                        response.getStatus(), response.getMessage());
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
                throw new ServerException(ExceptionStatus.PROPOSAL_RESPONSE_INTERCEPTOR_ERROR);
            }
        }
        return FabricProposalResponse.of(r);
    }

    private Channel.TransactionOptions channelTxOptions() {
        checkChannel();
        return new Channel.TransactionOptions()
                .orderers(curChannel.getOrderers())
                .userContext(hfClient.getUserContext())
                .nOfEvents(Channel.NOfEvents.nofNoEvents);
    }

    public List<Transaction> getTransactions(long fromBlock, long toBlock) throws TransactionException {

        List<Transaction> transactions = null;

        BlockchainInfo[] blockchainInfo = queryBlockchainInfo();
        long lastBlock = blockchainInfo[0].getHeight();
        if (lastBlock < fromBlock) {
            throw new IllegalArgumentException("현재 block(" + lastBlock + ")이 from(" + fromBlock + ") 보다 작습니다.");
        }
        long endBlock = toBlock;
        if (toBlock < lastBlock) endBlock = lastBlock;

        transactions = new ArrayList<>();

        for (long h = fromBlock; h < endBlock; h++) {
            BlockInfo blockInfo = queryBlockByNumber(h);

            for (BlockInfo.EnvelopeInfo envelopeInfo : blockInfo.getEnvelopeInfos()) {
                if (envelopeInfo.getType() == BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE) {

                    BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;

                    String txId = transactionEnvelopeInfo.getTransactionID();
                    boolean isValid = transactionEnvelopeInfo.isValid();


                    Iterator<BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo> transactionActionInfoIterator =
                            transactionEnvelopeInfo.getTransactionActionInfos().iterator();

                    List<TransactionAction> actions = new ArrayList<>();

                    while (transactionActionInfoIterator.hasNext()) {
                        BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo = transactionActionInfoIterator.next();

                        String chaincodeName = transactionActionInfo.getChaincodeIDName();
                        String chaincodeVersion = transactionActionInfo.getChaincodeIDVersion();

                        List<String> args = new ArrayList<>();

                        int argsCount = transactionActionInfo.getChaincodeInputArgsCount();
                        if (argsCount > 0) {
                            for (int i = 0; i < argsCount; i++) {
                                args.add(new String(transactionActionInfo.getChaincodeInputArgs(i), StandardCharsets.UTF_8));
                            }
                        }

                        actions.add(TransactionAction.builder()
                                .chaincodeName(chaincodeName)
                                .chaincodeVersion(chaincodeVersion)
                                .args(args)
                                .build());

                    }

                    transactions.add(Transaction.builder()
                            .blockNumber(h)
                            .txId(txId)
                            .isValid(isValid)
                            .actions(actions)
                            .build());
                }

            }
        }

        return transactions;
    }
}
