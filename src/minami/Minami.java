/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minami;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 *
 * @author root
 */
public class Minami {
    
    private static Logger logger = Logger.getLogger(Minami.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            
            int PORT = 0;
            
            PORT = 8080;
            
            logger.log(Level.INFO,">>> Starting Gateway on port "+PORT+" <<<");
            
            IoAcceptor acceptor = new NioSocketAcceptor();
            
            acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );
            
            acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ))));
                      
            acceptor.setCloseOnDeactivation(true);
            
            acceptor.setHandler(new IsoHandler());
            acceptor.getSessionConfig().setReadBufferSize( 2048 );
            acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
            
            ////////////set tcp no delay 20150608//////////////
            SocketSessionConfig sessionConf = (SocketSessionConfig) acceptor.getSessionConfig();
            sessionConf.setReuseAddress(true);
            sessionConf.setTcpNoDelay(true);
            ///////////////////////////////////////////////////
            
            acceptor.bind( new InetSocketAddress(PORT) );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
