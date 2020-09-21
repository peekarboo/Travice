
package org.travice.protocol;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.travice.BaseHttpProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.DateUtil;
import org.travice.model.Position;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;

public class SpotProtocolDecoder extends BaseHttpProtocolDecoder {

    private DocumentBuilder documentBuilder;
    private XPath xPath;
    private XPathExpression messageExpression;

    public SpotProtocolDecoder(SpotProtocol protocol) {
        super(protocol);
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xPath = XPathFactory.newInstance().newXPath();
            messageExpression = xPath.compile("//messageList/message");
        } catch (ParserConfigurationException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;

        Document document = documentBuilder.parse(new ByteBufferBackedInputStream(request.content().nioBuffer()));
        NodeList nodes = (NodeList) messageExpression.evaluate(document, XPathConstants.NODESET);

        List<Position> positions = new LinkedList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, xPath.evaluate("esnName", node));
            if (deviceSession != null) {

                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                position.setValid(true);
                position.setTime(DateUtil.parseDate(xPath.evaluate("timestamp", node)));
                position.setLatitude(Double.parseDouble(xPath.evaluate("latitude", node)));
                position.setLongitude(Double.parseDouble(xPath.evaluate("longitude", node)));

                position.set(Position.KEY_EVENT, xPath.evaluate("messageType", node));

                positions.add(position);

            }
        }

        sendResponse(channel, HttpResponseStatus.OK);
        return positions;
    }

}
