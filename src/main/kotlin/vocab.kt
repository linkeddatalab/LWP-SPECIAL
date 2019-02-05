package net.cityspin.vocab

import org.apache.jena.rdf.model.ResourceFactory

//static vocabulary for CPSS-SPECIAL
object CPSS {
	const val NS = "http://w3id.org/cityspin/ns/special-cpss#"

	val Temperature 		= get("Temperature")
	val EnergyConsumption 	= get("EnergyConsumption")

	// properties
	val hasConfig 	= getProperty("hasConfig")
	val refPolicy 	= getProperty("refPolicy")
	val userPolicy 	= getProperty("userPolicy")
	val usagePolicy = getProperty("usagePolicy")
	val userSubj 	= getProperty("userSubj")
	val usageSubj 	= getProperty("usageSubj")

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)
}

object LW {
	const val NS = "http://linkedwidgets.org/ns/ontology#"

	val Widget 			= get("Widget")
	val Wiring 			= get("Wiring")
	val Mashup 			= get("Mashup")
	val InputWidget 	= get("InputWidget")
	val ProcessWidget 	= get("ProcessWidget")

	val hasWidget 		= getProperty("hasWidget")
	val hasWiring 		= getProperty("hasWiring")
	val wiringInput 	= getProperty("wiringInput")
	val wiringOutput 	= getProperty("wiringOutput")

	val hasInputData 	= getProperty("hasInputData")
	val hasOutputData 	= getProperty("hasOutputData")
	val hasStorage 		= getProperty("hasStorage")
	val hasProcessing 	= getProperty("hasProcessing")
	val hasPurpose 		= getProperty("hasPurpose")
	val hasRecipient 	= getProperty("hasRecipient")

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)	
}

object LWR {
	const val NS = "http://linkedwidgets.org/ns/resource/"

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)	
}