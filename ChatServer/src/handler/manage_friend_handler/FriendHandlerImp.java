package handler.manage_friend_handler;

import java.util.List;

import data_access.person_access.PersonDAO;
import handler.Handler;
import model.communication.CPackage;
import model.communication.Name;
import model.communication.Request;
import model.communication.Type;
import model.converter.PersonConverter;
import model.sendmodel.ConfirmFriendModel;
import model.sendmodel.Person;
import socket.Client;

public class FriendHandlerImp extends Handler implements FriendHandler {

	private PersonDAO dao;

	// --------------------------------------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------------- Constructor
	// --------------------------------------------------------------------------------------------------------------------------------------------

	public FriendHandlerImp(Client client, PersonDAO dao) {
		super(client);
		this.dao = dao;
	}

	// --------------------------------------------------------------------------------------------------------------------------------------------
	// -------------------------------------------------------------- Handle Request
	@Override
	public void handleRequest(Request request) {
		if (request.isValid()) {
			Request responseCommandType = null;

			Name command = request.getName();
			switch (command) {
			case GET:
				responseCommandType = new Request(Name.GET, get(client.getPerson().getId()));
				break;
			case FIND:
				responseCommandType = new Request(Name.FIND, find(request.getContent().toString()));
				break;
			case ADD:
				responseCommandType = new Request(Name.ADD, add((int)request.getContent()));
				break;
			case CONFIRM:
				responseCommandType = new Request(Name.CONFIRM, confirm((ConfirmFriendModel)request.getContent()));
				break;
			case REMOVE:
				responseCommandType = new Request(Name.REMOVE, remove((Person)request.getContent()));
				break;
			default:
				responseCommandType = new Request(null, null);
				break;
			}

			packAndSend(responseCommandType);
		}

	}

	// --------------------------------------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------ Pack And Send
	@Override
	public void packAndSend(Request request) {
		if (request.isValid()) {
			CPackage CPackage = new CPackage(Type.FRIEND, request);
			send(CPackage);
		}
	}

	public void packAndSendTo(Client client, Request request) {
		if (request.isValid()) {
			CPackage CPackage = new CPackage(Type.FRIEND, request);
			client.send(CPackage);
		}
	}

	// --------------------------------------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------------- Get

	@Override
	public List<Person> get(int id) {
		List<Person> friendList = new PersonConverter().convert(dao.getListByID_Friend(id));
		return friendList;
	}

	// --------------------------------------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------------- Find

	@Override
	public Person find(String phonenumber) {
		Person person = null;
		if(phonenumber != null && phonenumber.length() == 10)
		{
			person = new PersonConverter().convert(dao.getByPhonenumber(phonenumber));
		}
		return person;
	}

	// --------------------------------------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------------- Add

	@Override
	public boolean add(int id) {
		boolean success = false;
		if (id > 0) {
			// Send request if online
			Client clientFriend = authorizedClientList.get(id);
			if (id != client.getPerson().getId() && clientFriend != null) {
				packAndSendTo(clientFriend, new Request(Name.CONFIRM, new ConfirmFriendModel(client.getPerson())));
				success = true;
			}
		}

		return success;
	}

	@Override
	public boolean confirm(ConfirmFriendModel sender) {
		boolean success = false;
		if (sender.isValid()) {
			// Send request if online
			Client clientFriend = authorizedClientList.get(sender.getSender().getId());
			if (sender.getSender().getId() != client.getPerson().getId() && clientFriend != null) {
				
				//Notify sender
				Thread notify = new Thread(()->
				{
					packAndSendTo(clientFriend, new Request(Name.CONFIRM, sender));
				});
				notify.start();
				
				// Update database if accept friend request
				if(sender.getIsFriend())
				{
					Thread update = new Thread(()->
					{
						dao.accept(client.getPerson().getId(), sender.getSender().getId());
					});
					update.start();
				}
				success = true;
			}
		}

		return success;
	}

	// --------------------------------------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------------- Remove

	@Override
	public boolean remove(Person person) {
		boolean success = false;

		if (person.isValid()) {
			success = dao.removeFriend(client.getPerson().getId(), person.getId());
		}

		return success;
	}
}
