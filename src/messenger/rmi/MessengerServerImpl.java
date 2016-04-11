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

	@Override
	public boolean createGroup(String nameGroup) throws RemoteException {
		// Cria um grupo
		GroupImpl group = new GroupImpl();
		// Obtém o token com o nome do grupo
		// Seta o grupo criado com parâmetro recebido
		group.setNameGroup(nameGroup);
		// Se o grupo não está na lista
		if (!this.groups.contains(group)) {
			// Adiciona o grupo criado na lista de grupos
			this.groups.add(group);
			return true;
		} else {
			System.err.println("Existing user!");
			return false;
		}
	}

	@Override
	public boolean addUser(String groupName, String member) throws RemoteException {
		// Verifica se esse grupo existe
		if(this.groups.contains(groupName)){
			GroupImpl group = getGroupImpl(groupName);
			// Verifica se o usuário foi criado e adiciona-o no grupo criado
			if(this.nameUsers.contains(member)){
				MessengerClient client = this.users.get(member);
				group.addMembers(client, member);
				return true;
			} else {
				System.err.println("User does not exist!");
				return false;
			}
		}
		System.err.println("Group does not exist!");
		return false;
	}

	public GroupImpl getGroupImpl(String name){
		for (GroupImpl group : this.groups){
			if(group.getNameGroup().equals(name)){
				return group;
			}
		}
		return null;
	}

	@Override
	public boolean msgGroup(String fromClient, String groupName, String msg) throws RemoteException {
		GroupImpl group = this.getGroupImpl(groupName);
		if(group != null){
			// Itera no map de usuários para enviar mensagem para cada um
			for(Map.Entry<String, MessengerClient> entry : group.getMembers().entrySet()){
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
		} else{
			return false;
		}
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