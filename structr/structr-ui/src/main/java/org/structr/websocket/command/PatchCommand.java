/*
 *  Copyright (C) 2011 Axel Morgner
 *
 *  This file is part of structr <http://structr.org>.
 *
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */



package org.structr.websocket.command;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.structr.core.entity.AbstractNode;
import org.structr.web.entity.Content;
import org.structr.websocket.message.MessageBuilder;
import org.structr.websocket.message.WebSocketMessage;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.websocket.StructrWebSocket;
import org.structr.websocket.command.AbstractCommand;
import org.structr.websocket.command.AbstractCommand;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Axel Morgner
 */
public class PatchCommand extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(PatchCommand.class.getName());

	//~--- methods --------------------------------------------------------

	@Override
	public void processMessage(WebSocketMessage webSocketData) {

		AbstractNode node              = getNode(webSocketData.getId());
		Map<String, Object> properties = webSocketData.getNodeData();
		String patch                   = (String) properties.get("patch");

		if (node != null) {

			diff_match_patch dmp      = new diff_match_patch();
			String oldText            = node.getStringProperty(Content.UiKey.content);
			LinkedList<Patch> patches = (LinkedList<Patch>) dmp.patch_fromText(patch);
			Object[] results          = dmp.patch_apply(patches, oldText);

			try {
				node.setProperty(Content.UiKey.content, results[0].toString());
			} catch (Throwable t) {

				logger.log(Level.WARNING, "Could not apply patch {0}", patch);
				getWebSocket().send(MessageBuilder.status().code(400).build(), true);
			}

		} else {

			logger.log(Level.WARNING, "Node with uuid {0} not found.", webSocketData.getId());
			getWebSocket().send(MessageBuilder.status().code(404).build(), true);

		}
	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getCommand() {
		return "PATCH";
	}
}