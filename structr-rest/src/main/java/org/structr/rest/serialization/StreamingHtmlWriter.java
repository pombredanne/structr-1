package org.structr.rest.serialization;

import java.io.Writer;
import org.structr.core.Value;

/**
 *
 * @author Christian Morgner
 */
public class StreamingHtmlWriter extends StreamingWriter {

	public StreamingHtmlWriter(final Value<String> propertyView, final boolean indent) {
		super(propertyView, indent);
	}
	
	@Override
	public RestWriter getRestWriter(Writer writer) {
		
		return new StructrJsonHtmlWriter(writer);
	}
}