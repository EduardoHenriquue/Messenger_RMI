package messenger.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class MessengerClientImpl implements MessengerClient {

	private String userName;
	private static String COMMANDS = "Commands: \n" +
									 "\t login \n" +
			                         "\t users \n" +
									 "\t msg <username> <message> \n" +
			                         "\t broadcast <message> \n" +
									 "\t create-group <groupname> <username-1> <username-n> \n" +
									 "\t group-msg <groupname> <message> \n" +
									 "\t add-members-in-group <groupname> <username-1> <username-n> \n" +
									 "\t logout";
	private static String INIT_CHAR = "$ ";
	
	
	public MessengerClientImpl(String userName) {
		this.userName = userName;
	}

	@Override
	public void receiveMsg(String from, String msg) throws RemoteException {
		System.out.println();
		System.out.println("<" + from + "> " + msg);
		System.out.print(INIT_CHAR);
	}

	private static void handleMsg(String userName, MessengerServer server,
			StringTokenizer lineTokenizer) throws RemoteException {
		String to = lineTokenizer.nextToken();
		StringBuilder msg = new StringBuilder();
		while (lineTokenizer.hasMoreTokens()) {
			msg.append(lineTokenizer.nextToken() + " ");
		}
		if (!server.sendMsg(userName, to, msg.toString())) {
			System.err.println("Message not sent!" +
					"\nUser does not exist or is not logged in.");
		}
	}

	/**
	 *
	 * @param userName
	 * @param server
	 * @param lineTokenizer
	 * @throws RemoteException
     */
	private static void handleMsgBroadcast(String userName, MessengerServer server,
								  StringTokenizer lineTokenizer) throws RemoteException {
		StringBuilder msg = new StringBuilder();
		while (lineTokenizer.hasMoreTokens()) {
			msg.append(lineTokenizer.nextToken() + " ");
		}
		if (!server.broadcast(userName, msg.toString())) {
			System.err.println("Message not sent!");
		}
	}

	/**
	 *  Envia uma mensagem para os usuários de um grupo
	 * @param userName
	 * @param server
	 * @param lineTokenizer
	 * @throws RemoteException
     */
	private static void handleMsgGroup(String userName, MessengerServer server, StringTokenizer lineTokenizer) throws RemoteException {
		String groupName = lineTokenizer.nextToken();
		StringBuilder msg = new StringBuilder();
		while (lineTokenizer.hasMoreTokens()) {
			msg.append(lineTokenizer.nextToken() + " ");
		}
		if (!server.msgGroup(userName, groupName, msg.toString())) {
			System.err.println("Message not sent!");
		}
	}

	private static void handleCreateGroup(MessengerServer server,StringTokenizer lineTokenizer) throws RemoteException {
		String groupName = lineTokenizer.nextToken();
		server.createGroup(groupName);
		while (lineTokenizer.hasMoreTokens()) {
			// Lê o próximo token
			String userName = lineTokenizer.nextToken();
			// Verifica se o usuário foi criado e adiciona-o no grupo criado
			if(!server.addUser(groupName, userName)){
				System.err.println("User does not exist!");
			}
		}

	}

	private static void handleAddUser(MessengerServer server,StringTokenizer lineTokenizer) throws RemoteException{
		String groupName = lineTokenizer.nextToken();
		while (lineTokenizer.hasMoreTokens()){
			// Lê o próximo token
			String userName = lineTokenizer.nextToken();
			if (!server.addUser(groupName, userName)){
				System.err.println("User does not exist!" +
						"\nOR" +
						"\nGroup does not exist!");
			}
		}

	}



	public static void main(String[] args) {
		try {
			String userName = args[0];
			String host = "localhost";
			MessengerClient client = new MessengerClientImpl(userName);
			MessengerClient stub = (MessengerClient) UnicastRemoteObject
					.exportObject(client, 0);
			Registry registry = LocateRegistry.getRegistry(host);
			MessengerServer server = (MessengerServer) registry
					.lookup("MessengerServer");
			Scanner input = new Scanner(System.in);
			String line = "";
			System.out.println(COMMANDS);
			System.out.print(INIT_CHAR);
			while (!(line = input.nextLine().trim().toLowerCase())
					.equals("exit")) {
				
				StringTokenizer lineTokenizer = new StringTokenizer(line, " ");
				if (lineTokenizer.hasMoreTokens()) {
					String command = lineTokenizer.nextToken();
					if (command.equals("login")) {
						server.login(client, userName);
					}
					else if (command.equals("msg")) {
						handleMsg(userName, server, lineTokenizer);
					}
					else if (command.equals("users")) {
						// Se o usuário estiver logado, ele poderá ver a lista de usuários
						if(server.isUserLogged(userName)){
							System.out.println(server.listUsers());
						} else {
							System.err.println("You are not logged in!");
						}
					}
					else if(command.equals("broadcast")) {
						handleMsgBroadcast(userName, server, lineTokenizer);
					}
					else if(command.equals("create-group")){
						handleCreateGroup(server, lineTokenizer);
					}
					else if(command.equals("group-msg")){
						handleMsgGroup(userName, server, lineTokenizer);
					}
					else if(command.equals("add-members-in-group")){
						handleAddUser(server,lineTokenizer);
					}
					else if (command.equals("logout")) {
						server.logout(userName);
					}
					else {
						System.err.println("Unknown command: " + command + "\n" + COMMANDS);
					}
				}
				System.out.print("$ ");
			}
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
