//package example;
//
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
//
//import java.util.logging.Logger;
//
//public class TestLogger implements LambdaLogger {
//	
//	//private static final Logger logger =  LoggerFactory.getLogger(TestLogger.class);
//	private static final Logger logger = Logger.getLogger(TestLogger.class.getName());
//	@Override
//	public void log(String message) {
//		// TODO Auto-generated method stub
//		logger.info(message);
//	}
//
//	@Override
//	public void log(byte[] message) {
//		// TODO Auto-generated method stub
//		logger.info(new String(message));
//	}
//}
