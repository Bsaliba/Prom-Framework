package org.processmining.framework.plugin.events;

import java.util.EventListener;

import org.processmining.framework.connections.ConnectionID;

public interface ConnectionObjectListener extends EventListener {

	public class ListenerList extends ProMEventListenerList<ConnectionObjectListener> {
		public void fireConnectionCreated(ConnectionID connectionID) {
			for (ConnectionObjectListener listener : getListeners()) {
				listener.connectionCreated(connectionID);
			}
		}

		public void fireConnectionDeleted(ConnectionID id) {
			for (ConnectionObjectListener listener : getListeners()) {
				listener.connectionDeleted(id);
			}
		}
	}

	public void connectionCreated(ConnectionID connectionID);

	public void connectionDeleted(ConnectionID connectionID);
}
