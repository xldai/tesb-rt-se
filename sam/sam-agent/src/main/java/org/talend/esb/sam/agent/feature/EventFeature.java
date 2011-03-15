package org.talend.esb.sam.agent.feature;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.talend.esb.sam.agent.interceptor.EventProducerInterceptor;
import org.talend.esb.sam.agent.interceptor.FlowIdProducerIn;
import org.talend.esb.sam.agent.interceptor.FlowIdProducerOut;
import org.talend.esb.sam.agent.interceptor.WireTapIn;
import org.talend.esb.sam.agent.interceptor.WireTapOut;
import org.talend.esb.sam.agent.interceptor.soap.FlowIdSoapCodec;
import org.talend.esb.sam.agent.interceptor.transport.FlowIdTransportCodec;
import org.talend.esb.sam.agent.mapper.MessageToEventMapper;
import org.talend.esb.sam.common.spi.EventManipulator;

/**
 * Feature adds FlowIdProducer Interceptor and EventProducer Interceptor.
 * 
 * @author cschmuelling
 */
public class EventFeature extends AbstractFeature {

    private MessageToEventMapper mapper;
    private EventManipulator eventSender;
    private boolean logMessageContent;

    public EventFeature() {
        super();
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        super.initializeProvider(provider, bus);

        FlowIdProducerIn<Message> flowIdProducerIn = new FlowIdProducerIn<Message>();
        FlowIdProducerOut<Message> flowIdProducerOut = new FlowIdProducerOut<Message>();

        FlowIdSoapCodec flowIdSoapCodec = new FlowIdSoapCodec();
        FlowIdTransportCodec flowIdHttpCodecIn = new FlowIdTransportCodec(Phase.READ);
        FlowIdTransportCodec flowIdHttpCodecOut = new FlowIdTransportCodec(Phase.USER_PROTOCOL);

        EventProducerInterceptor epi = new EventProducerInterceptor(mapper, eventSender);
        
        WireTapIn wireTapIn = new WireTapIn(logMessageContent);
        provider.getInInterceptors().add(wireTapIn);
        provider.getInInterceptors().add(epi);
        WireTapIn wireTapInFault = new WireTapIn(logMessageContent);
        provider.getInFaultInterceptors().add(wireTapInFault);
        provider.getInFaultInterceptors().add(epi);

        WireTapOut wireTapOut = new WireTapOut(epi, logMessageContent);
        provider.getOutInterceptors().add(wireTapOut);
        WireTapOut wireTapOutFault = new WireTapOut(epi, logMessageContent);
        provider.getOutFaultInterceptors().add(wireTapOutFault);

        // Add FlowIdProducer
        provider.getInInterceptors().add(flowIdProducerIn);
        provider.getInInterceptors().add(flowIdSoapCodec);
        provider.getInInterceptors().add(flowIdHttpCodecIn);
        provider.getInFaultInterceptors().add(flowIdProducerIn);
        provider.getInFaultInterceptors().add(flowIdSoapCodec);
        provider.getInFaultInterceptors().add(flowIdHttpCodecIn);
        provider.getOutInterceptors().add(flowIdProducerOut);
        provider.getOutInterceptors().add(flowIdSoapCodec);
        provider.getOutInterceptors().add(flowIdHttpCodecOut);
        provider.getOutFaultInterceptors().add(flowIdProducerOut);
        provider.getOutFaultInterceptors().add(flowIdSoapCodec);
        provider.getOutFaultInterceptors().add(flowIdHttpCodecOut);

        // add dependencies for incoming messages
        flowIdProducerIn.addAfter(WireTapIn.class.getName());
        wireTapIn.addBefore(FlowIdProducerIn.class.getName());

        // add dependencies for outgoing messages
        flowIdProducerOut.addAfter(WireTapOut.class.getName());
        wireTapOut.addBefore(FlowIdProducerOut.class.getName());


    }

    public void setMapper(MessageToEventMapper mapper) {
        this.mapper = mapper;
    }

    public void setEventSender(EventManipulator eventSender) {
        this.eventSender = eventSender;
    }

    public void setLogMessageContent(boolean logMessageContent) {
        this.logMessageContent = logMessageContent;
    }
    
}