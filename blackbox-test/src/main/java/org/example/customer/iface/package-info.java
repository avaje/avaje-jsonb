@Json.Import(value = CIFace.class, jsonSettings = @Json(naming = Json.Naming.LowerHyphen))
@Json.Import(value = DIFace.class, implementation = MyDIFace.class)
package org.example.customer.iface;

import org.example.customer.iface.implementation.MyDIFace;

import io.avaje.jsonb.Json;
