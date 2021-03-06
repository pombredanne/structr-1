/**
 * Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
 *
 * This file is part of structr <http://structr.org>.
 *
 * structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.structr.core.graph;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.structr.core.GraphObjectMap;
import org.structr.core.property.GenericProperty;

//~--- classes ----------------------------------------------------------------

/**
 * Executes the given Cypher query and tries to convert the result in a List
 * of {@link GraphObject}s.
 *
 * @author Christian Morgner
 */
public class CypherQueryCommand extends NodeServiceCommand {

	private static final Logger logger = Logger.getLogger(CypherQueryCommand.class.getName());
	
	//protected static final ThreadLocalExecutionEngine engine = new ThreadLocalExecutionEngine();
	
	//~--- methods --------------------------------------------------------

	public List<GraphObject> execute(String query) throws FrameworkException {
		return execute(query, null);
	}

	public List<GraphObject> execute(String query, Map<String, Object> parameters) throws FrameworkException {
		return execute(query, parameters, false);
	}

	public List<GraphObject> execute(String query, Map<String, Object> parameters, boolean includeHiddenAndDeleted) throws FrameworkException {
		return execute(query, parameters, includeHiddenAndDeleted, false);
	}
	
	public List<GraphObject> execute(String query, Map<String, Object> parameters, boolean includeHiddenAndDeleted, boolean publicOnly) throws FrameworkException {

		ExecutionEngine     engine      = (ExecutionEngine) arguments.get("cypherExecutionEngine");
		RelationshipFactory relFactory  = new RelationshipFactory(securityContext);
		NodeFactory nodeFactory         = new NodeFactory(securityContext);

		List<GraphObject> resultList = new LinkedList<GraphObject>();
		ExecutionResult result       = null;

		if (parameters != null) {

			result = engine.execute(query, parameters);
			
		} else {
			
			result = engine.execute(query);
		}

		for (Map<String, Object> row : result) {

			GraphObjectMap dummyObject = null;
			
			for (Entry<String, Object> entry : row.entrySet()) {
				
				String key   = entry.getKey();
				Object value = entry.getValue();
			
				if (value instanceof Node) {

					AbstractNode node = nodeFactory.instantiate((Node) value, includeHiddenAndDeleted, publicOnly);

					if (node != null) {

						resultList.add(node);
					}

				} else if (value instanceof Relationship) {

					AbstractRelationship rel = relFactory.instantiate((Relationship) value);

					if (rel != null) {

						resultList.add(rel);
					}

				} else {
					
					if (dummyObject == null) {
						
						dummyObject = new GraphObjectMap();
						resultList.add(dummyObject);
					}
						
					dummyObject.setProperty(new GenericProperty(key), value);
				}

			}

		}

		return resultList;
	}

}
