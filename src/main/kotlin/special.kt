package net.cityspin.vocab.special

import org.apache.jena.rdf.model.ResourceFactory

object SPL {
	const val NS = "https://www.specialprivacy.eu/langs/usage-policy#"

	// properties
	val hasData 		= getProperty("hasData")
	val hasProcessing 	= getProperty("hasProcessing")
	val hasPurpose 		= getProperty("hasPurpose")
	val hasRecipient 	= getProperty("hasRecipient")
	val hasStorage 		= getProperty("hasStorage")

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)
}

object SVD {
	const val NS = "http://www.specialprivacy.eu/vocabs/data#"

	val AnyData 	= get("AnyData")
	val UniqueId 	= get("UniqueId")
	val Location 	= get("Location")

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)
}

object SVPR {
	const val NS = "http://www.specialprivacy.eu/vocabs/processing#"

	val Aggregate 	= get("Aggregate")
	val Analyze 	= get("Analyze")
	val Anonymize 	= get("Anonymize")
	val Transfer 	= get("Transfer")

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)
}

object SVPU {
	const val NS = "http://www.specialprivacy.eu/vocabs/purposes#"

	val Develop  = get("Develop")

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)
}

object SVR {
	const val NS = "http://www.specialprivacy.eu/vocabs/recipients#"

	val Ours 		= get("Ours")
	val Public 		= get("Public")

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)
}

object SVL {
	const val NS = "http://www.specialprivacy.eu/vocabs/locations#"

	val AT 		= get("AT")
	val EU 		= get("EU")

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)
}