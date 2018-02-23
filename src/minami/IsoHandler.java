package minami;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

public class IsoHandler extends IoHandlerAdapter {

    String trailer = new String(new char[]{255});
    String secret_key = "";

    private static Logger logger = Logger.getLogger(IsoHandler.class);

    public String sendISO(String IP_SERVER, String PORT_SERVER, String networkRequest) throws UnknownHostException, IOException {

        String hasil = "";
        logger.log(Level.INFO, "SENDING : " + networkRequest);

        try {

            Socket clientSocket = new Socket(IP_SERVER, Integer.parseInt(PORT_SERVER));

            PrintWriter outgoing = new PrintWriter(clientSocket.getOutputStream());
            InputStreamReader incoming = new InputStreamReader(clientSocket.getInputStream());

            String trailer = new String(new char[]{10});

            //kirim
            outgoing.print(networkRequest + trailer);
            outgoing.flush();

            int data;
            StringBuffer sb = new StringBuffer();

            while ((data = incoming.read()) != 10) {
                if (data == -1 || data == 255 || data == 65533) {
                    break;
                }

                sb.append((char) data);
            }

            logger.log(Level.INFO, "Rec. Msg => " + sb.toString());

            outgoing.close();
            incoming.close();
            clientSocket.close();

            hasil = sb.toString();
        } catch (Exception e) {
            logger.log(Level.FATAL, e);

            try {
                GenericPackager packager = new GenericPackager("packager/isoSLSascii.xml");
                ISOMsg isoMsg = new ISOMsg();
                isoMsg.setPackager(packager);

                isoMsg.setMTI((String.valueOf(Integer.parseInt(isoMsg.getMTI().toString()) + 10)));
                isoMsg.set(2, isoMsg.getString(2));
                isoMsg.set(11, "0");
                isoMsg.set(4, isoMsg.getString(4));
                isoMsg.set(39, "0068");

                byte[] datax = isoMsg.pack();
                hasil = new String(datax);
                logger.log(Level.INFO, "ISO RESULT : " + new String(datax));

            } catch (Exception isox) {
                logger.log(Level.FATAL, "ISO : " + isox.getMessage());
            }
        }

        return hasil;

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("IDLE " + session.getIdleCount(status));
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        //receive ISO message
        String str = message.toString();
        String respon = "";
        logger.log(Level.INFO, "Receive from CA : " + str);

        //parse ISO Message
        try {
            GenericPackager packager = new GenericPackager("packager/isoSLSascii.xml");
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.unpack(str.getBytes());

            if (isoMsg.getMTI().equalsIgnoreCase("2800")) {
                logger.log(Level.INFO, "ECHO MSG");
                try {

                    isoMsg.setMTI("2810");
                    logger.log(Level.INFO, "2810");
                    isoMsg.set(11, isoMsg.getString(11));
                    isoMsg.set(12, isoMsg.getString(12));
                    isoMsg.set(39, "0000");
                    isoMsg.set(40, isoMsg.getString(40));
                    isoMsg.set(41, isoMsg.getString(41));

                    byte[] datax = isoMsg.pack();

                    respon = new String(datax);

                    logger.log(Level.INFO, "respon netmsg : " + respon);

                } catch (Exception e) {
                    logger.log(Level.FATAL, "error netmsg " + e.getMessage());
                }
            }

            respon = respon + trailer;
            logger.log(Level.INFO, "RESPONSE : " + respon);

            session.write(respon.toString());

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.FATAL, e);

            try {
                GenericPackager packager = new GenericPackager("packager/isoSLSascii.xml");
                ISOMsg isoMsg = new ISOMsg();
                isoMsg.setPackager(packager);

                isoMsg.setMTI((String.valueOf(Integer.parseInt(isoMsg.getMTI().toString()) + 10)));
                isoMsg.set(2, isoMsg.getString(2));
                isoMsg.set(11, "0");
                isoMsg.set(4, isoMsg.getString(4));
                isoMsg.set(39, "0005");

                byte[] datax = isoMsg.pack();
                respon = new String(datax);
                logger.log(Level.INFO, "ISO RESULT : " + new String(datax));

            } catch (Exception isox) {
                logger.log(Level.FATAL, "ISO : " + isox.getMessage());
            }

        }
    }

}
