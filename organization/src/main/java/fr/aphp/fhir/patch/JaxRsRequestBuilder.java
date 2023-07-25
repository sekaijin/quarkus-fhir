/*
 * #%L
 * HAPI FHIR JAX-RS Server
 * %%
 * Copyright (C) 2014 - 2023 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package fr.aphp.fhir.patch;

import ca.uhn.fhir.jaxrs.server.AbstractJaxRsProvider;
import ca.uhn.fhir.jaxrs.server.util.JaxRsRequest;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;

/**
 * The JaxRsRequest is a jax-rs specific implementation of the RequestDetails.
 *
 * @author Peter Van Houte | peter.vanhoute@agfa.com | Agfa Healthcare
 */
public class JaxRsRequestBuilder extends JaxRsRequest.Builder {


		public JaxRsRequestBuilder(AbstractJaxRsProvider theServer, RequestTypeEnum theRequestType,
				RestOperationTypeEnum theRestOperation, String theRequestUrl, String theResourceName) {
			super(theServer, theRequestType, theRestOperation, theRequestUrl, theResourceName);
		}

		/**
		 * Create the jax-rs request
		 *
		 * @return the jax-rs request
		 */
		public JaxRsRequest build() {
			JaxRsRequest result = super.build();
			result.setRequestPath(result.getResourceName());
			return result;
		}
}
