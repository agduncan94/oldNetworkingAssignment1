import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;

public class Sender {
	public static String host, port, messageSize, fileName;
	public static void main(String args[]) throws IOException {
		// Parse arguments
		host = args[0];
		port = args[1];
		messageSize = args[2];
		fileName = args[3];

		int numMessagesSent = 0;
		int numBytesSent = 0;

		// Setup socket
		InetAddress inetAddress = InetAddress.getByName(host);
		DatagramSocket dSocket = new DatagramSocket();

		// Get file into byte array
		Path fileLocation = Paths.get(fileName);
		byte[] data = Files.readAllBytes(fileLocation);

		// Initial setup
		int maxMessageSize =  Integer.parseInt(messageSize);
		int totalDataSize = data.length;
		int remainingSize = totalDataSize;
		String lastMessage = "0";
		boolean messageSent = false;
		int counter = 0;
		int offset = 0;

		// Send message(s)
		// An indicator byte (0 or 1) is prepended to the front of each message
		// 0 indicates the message is not the final message to be sent
		// 1 indicates the messe is the final message to be sent
		while (messageSent == false) {
			// If last message, set to 1
			if (remainingSize <= maxMessageSize - 1) {
				lastMessage = "1";
			}

			// Calculate offset
			offset = counter * (maxMessageSize - 1);
			counter++;

			if (remainingSize < maxMessageSize - 1) {
				maxMessageSize = remainingSize + 1;
			}
			
			// Prepend indicator byte and set data for package
			byte[] tempData = Arrays.copyOfRange(data, offset, offset + maxMessageSize - 1);
			byte[] messageIndicator = {lastMessage.getBytes()[0]};
			byte[] destData = new byte[tempData.length + messageIndicator.length];
			System.arraycopy(messageIndicator, 0, destData, 0, messageIndicator.length);
			System.arraycopy(tempData, 0, destData, 1, tempData.length);
			DatagramPacket dPacket = new DatagramPacket(destData, maxMessageSize, inetAddress, Integer.parseInt(port));


			// Send packet
			dSocket.send(dPacket);

			// Update tracking variables
			numMessagesSent++;
			numBytesSent += maxMessageSize;
			remainingSize = remainingSize - (maxMessageSize-1);

			// Quit once all messages are sent
			if (remainingSize == 0) {
				messageSent = true;
			}
		}

		System.out.println(numMessagesSent + " " + numBytesSent);
	}
}