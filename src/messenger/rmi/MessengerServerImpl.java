package messenger.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class MessengerServerImpl implements MessengerServer {

	private HashMap<String, MessengerClient> users;
	private GroupImpl groupImpl;
	private List<GroupImpl> groups;
	private List<String> nameUsers;

	public MessengerServerImpl() {
		this.users = new HashMap<String, MessengerClient>();
		this.groups = new LinkedList<>();
		this.groupImpl = new GroupImpl();
		this.nameUsers = new LinkedList<>();
	}

	@Override
	public void login(MessengerClient client, String userName)
			throws RemoteException {
		System.out.println("Login: " + userName);
		users.put(userName, client);
		// Adiciona o usuário a um lista de usuário cadastrados que poderão receber mensagens mesmo estando offline
		this.nameUsers.add(userName);
	}

	@Override
	public boolean sendMsg(String from, String to, String msg)
			throws RemoteException {
		System.out.println("Sending msg from " + from + " to " + to);
		MessengerClient toClient = users.get(to);
		if (toClient == null) {
			return false;
		}
		// Verifica se o usuário está no map se estiver, ele pode enviar mensagem.
		// Quando um usuário faz login ele é adicionado no map.
		if(this.isUserLogged(from)){
			toClient.receiveMsg(from, msg);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String listUsers() throws RemoteException {
		System.out.println("Listing logged users.");
		return users.keySet().toString();
	}

	@Override
	public void logout(String userName) throws RemoteException {
		System.out.println("Logout: " + userName);
		users.remove(userName);
	}

	/**
	 * Envia mensagem para todos os usuários
	 * @param fromClient
	 * @param msg
     * @return
     */
	@Override
	public boolean broadcast(String fromClient, String msg) throws RemoteException {
		// Itera no map de usuários para enviar mensagem para cada um
		for(Map.Entry<String, MessengerClient> entry : this.users.entrySet()){
			// Obtém o usuário que irá receber a mensagem
			String toClient = entry.getKey();

			if(toClient == null){
				return false;
			}
			// Verificação para evitar que um usuário envie mensagem para ele mesmo
			if(!toClient.equals(fromClient)){
				// Envia mensagem para os usuários
				this.sendMsg(fromClient, toClient, msg);
			}

		}
		return true;
	}

	@Override
	public boolean isUserLogged(String userName){
		return this.users.containsKey(userName);
	}


	public void createGroup(StringTokenizer lineTokenizer) throws RemoteException {
		// Cria um grupo
		GroupImpl group = new GroupImpl();
		// Obtém o token com o nome do grupo
		String groupName = lineTokenizer.nextToken();
		// Seta o grupo criado com o token obtido
		group.setNameGroup(groupName);

		while (lineTokenizer.hasMoreTokens()) {
			// Lê o próximo token
			String userName = lineTokenizer.nextToken();
			// Verifica se o usuário foi criado e adiciona-o no grupo criado
			if(this.nameUsers.contains(userName)){
				MessengerClient client = this.users.get(userName);
				group.addMembers(client, userName);
			} else {
				System.err.println("User does not exist!");
			}
		}
		// Adiciona o grupo criado na lista de grupos
		this.groups.add(group);
	}


	public void addUser(MessengerClient client) throws RemoteException {

	}


	public String listGroups() throws RemoteException {
		return null;
	}


	public static void main(String[] args) {
		try {
			String host = "127.0.0.1";
			LocateRegistry.createRegistry(1099);
			MessengerServerImpl obj = new MessengerServerImpl();
			MessengerServer stub = (MessengerServer) UnicastRemoteObject
					.exportObject(obj, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(host);
			registry.rebind("MessengerServer", stub);

			System.err.println("Server is running...");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}