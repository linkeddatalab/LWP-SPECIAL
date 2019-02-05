package net.cityspin.privacy;

import java.io.FileOutputStream

import org.apache.jena.query.ARQ
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.RDF

import openllet.jena.PelletReasonerFactory

import net.cityspin.vocab.CPSS
import net.cityspin.vocab.LWR

import kotlin.system.measureTimeMillis


fun checkCompliance(refPolicyFiles: List<String>, 
	userPolicyFile: String, userPolicyRes: String, 
	controllerPolicyFile: String, controllerPolicyRes: String): Boolean {
	try {
		val refModel = ModelFactory.createDefaultModel()

		for(refFn in refPolicyFiles) { 
			println("loading "+refFn)
			RDFDataMgr.read(refModel,refFn)
		}

		println("loading "+userPolicyFile)
		RDFDataMgr.read(refModel,userPolicyFile)

		println("loading "+controllerPolicyFile)
		RDFDataMgr.read(refModel,controllerPolicyFile)
		
		val reasoner = PelletReasonerFactory.theInstance().create()
		var status = false
		val checkingTime = measureTimeMillis {
        	val eModel = ModelFactory.createInfModel(reasoner, refModel)
       		status = eModel.contains(
       		ResourceFactory.createResource(userPolicyRes), 
       		RDFS.subClassOf, 
       		ResourceFactory.createResource(controllerPolicyRes) )
       	}
       	println("checked "+userPolicyRes+" < "+controllerPolicyRes+" in "+checkingTime+" ms : "+status)
       	return status

	}
	catch(e: Exception) {
		println("Something is not right: "+e)
	}
	return false
}

fun parseList(listNode:Resource):List<Resource> {
	if(listNode.equals(RDF.nil))
		return listOf()
	else {
		val vals = ArrayList<Resource>()
		var node = listNode
		do {
			vals.add(node.getProperty(RDF.first).getResource())
			node = node.getProperty(RDF.rest).getResource()
		} while (node != RDF.nil)
		return vals
	}
}

fun checkFromConfig(configFile: String) {
	val model = ModelFactory.createDefaultModel()
	RDFDataMgr.read(model, configFile)

	val root = model.listResourcesWithProperty(CPSS.hasConfig).toList()[0]
	println("using configuration from "+root)
	val nn: RDFNode? = null
	for (stmt in model.listStatements(root, CPSS.hasConfig, nn)) {
		//val cfg = model.getProperty(tmp, CPSS.hasConfig).getResource()
		val cfg = stmt.getResource()
		//println(cfg)

		val refModelFn = ArrayList<String>()
		for(refRes in parseList(model.getProperty(cfg, CPSS.refPolicy).getResource())) {
			refModelFn.add(refRes.getURI())
		}

		val userPolicyFn 	= model.getProperty(cfg, CPSS.userPolicy 	).getResource().getURI()
		val usagePolicyFn 	= model.getProperty(cfg, CPSS.usagePolicy 	).getResource().getURI()
		val userSubj 		= model.getProperty(cfg, CPSS.userSubj 		).getResource().getURI()
		val usageSubj 		= model.getProperty(cfg, CPSS.usageSubj 	).getResource().getURI()

		//TODO: update graph with provenance trail?
		checkCompliance(refModelFn, userPolicyFn, userSubj, usagePolicyFn, usageSubj)
	}
}

fun constructPolicy(ResName: String,
	dataPolicy:List<Resource>, 
	processPolicy: List<Resource>,
	purposePolicy: List<Resource>,
	recipientPolicy: List<Resource>,
	storagePolicy: List<Resource>,
	makeIntersection: Boolean = false
	): Model {
	val p = Policy(ResName)
	p.dataTypes.addAll(dataPolicy)
	p.processing.addAll(processPolicy)
	p.purpose.addAll(purposePolicy)
	p.recipient.addAll(recipientPolicy)
	p.storage.addAll(storagePolicy)
	return p.getModel(makeIntersection)	
}

fun testCompliance1() {
	val compliant = checkCompliance(listOf("data/cpss-special-integrated.ttl"),
		"data/user1policy.ttl", LWR.get("User1_Policy").getURI(),
		"data/Userlocationpolicy.ttl", LWR.get("UserLocation_Policy").getURI()
		)
	println(compliant)
}

fun testCompliance2() {
	val compliant = checkCompliance(listOf("data/cpss-special-integrated.ttl"),
		"data/user2policy.ttl", LWR.get("User2_Policy").getURI(),
		"data/Userlocationpolicy.ttl", LWR.get("UserLocation_Policy").getURI()
		)
	println(compliant)
}

fun main(args: Array<String>) {
	ARQ.init()//!important for loading files

	if(args.size==1 && args[0] == "-test") {
		println("total processing time (ms) : "+measureTimeMillis { testCompliance1() })
		println("total processing time (ms) : "+measureTimeMillis { testCompliance2() })
	}
	else if(args.size==2 && args[0] == "-check")
		println("total processing time (ms) : "+measureTimeMillis { checkFromConfig(args[1]) })
	else {
		println("Usage: privacycheck [-generate <mashup.ttl> <policy-out.ttl>] [-check <config.ttl>]")
	}
}
