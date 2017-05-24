import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;

public class Receiver {
	public static void main(String args[]) throws IOException {
		// Parse arguments
		int numMessagesRecieved = 0;
		int numBytesRecieved = 0;
		String filename = args[0];
		int timeout = Integer.parseInt(args[1]);

		// Create Socket
		DatagramSocket dSocket = new DatagramSocket(4445);
		// DatagramSocket dSocket = new DatagramSocket(0);
		byte[] data = new byte[65527];
		Path path =  Paths.get(filename);
		File outputFile = new File(filename);
		outputFile.createNewFile();

		// Print port to stdout and file
		String portFileName = "port";
		System.out.println(dSocket.getLocalPort());
		Path portPath = Paths.get(portFileName);
		File portFile = new File(portFileName);
		Files.write(portPath, ("" + dSocket.getLocalPort()).getBytes(), StandardOpenOption.CREATE);

		// Receive packets
		while(true) {
			try {
				// Create receiving packet
				DatagramPacket dPacket = new DatagramPacket(data, data.length);
				dSocket.receive(dPacket);
				dSocket.setSoTimeout(timeout);

				// Update variable trackers
				numMessagesRecieved++;
				numBytesRecieved += dPacket.getLength();

				// Get data and write to file
				byte[] pData = dPacket.getData();
				String output = new String(pData, "UTF-8");
				byte[] buffer = Arrays.copyOfRange(pData, 1, pData.length);
				Files.write(path, buffer, StandardOpenOption.APPEND);

				// Exit if the message is the last one
				if (output.charAt(0) == '1') {
					System.out.println(numMessagesRecieved + " " + numBytesRecieved);
					System.exit(0);
				}
			} catch (SocketTimeoutException ex) {
				System.out.println(numMessagesRecieved + " " + numBytesRecieved);
				System.exit(0);
			}
		}
	}
}