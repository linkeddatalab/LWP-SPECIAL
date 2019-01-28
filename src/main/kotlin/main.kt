package net.cityspin.privacy;

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths

import org.apache.jena.query.ARQ
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.Lang
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.OWL

import openllet.jena.PelletReasonerFactory

import net.cityspin.vocab.CPSS
import net.cityspin.vocab.LW

import net.cityspin.vocab.special.SPL
import net.cityspin.vocab.special.SVD
import net.cityspin.vocab.special.SVPR
import net.cityspin.vocab.special.SVPU
import net.cityspin.vocab.special.SVR
import net.cityspin.vocab.special.SVL

import kotlin.system.measureTimeMillis

object LWR {
	const val NS = "http://linkedwidgets.org/ns/resource/"

	//helper function
	fun get(name: String) = ResourceFactory.createResource(NS+name)
	fun getProperty(name: String) = ResourceFactory.createProperty(NS+name)	
}

class Policy (Uri: String) {
	val ResName = Uri
	val dataTypes = ArrayList<Resource>()
	val processing = ArrayList<Resource>()
	val storage = ArrayList<Resource>()
	val purpose = ArrayList<Resource>()
	val recipient = ArrayList<Resource>()

	fun getModel(makeIntersection: Boolean = false): Model {
		val model = ModelFactory.createDefaultModel()

		model.setNsPrefix("lw", LW.NS)
		model.setNsPrefix("lwr", LWR.NS)
		
		model.setNsPrefix("owl", OWL.NS)
		
		model.setNsPrefix("spl", SPL.NS)
		model.setNsPrefix("svd", SVD.NS)
		model.setNsPrefix("svpr", SVPR.NS)
		model.setNsPrefix("svpu", SVPU.NS)
		model.setNsPrefix("svr", SVR.NS)
		model.setNsPrefix("svl", SVL.NS)

		val dp = model.createResource()
		dp.addProperty(RDF.type, OWL.Restriction)
		dp.addProperty(OWL.onProperty, SPL.hasData)
		if(makeIntersection)
			dp.addProperty(OWL.intersectionOf, createList(model, dataTypes))
		else
			for(dataItem in dataTypes)
				dp.addProperty(OWL.someValuesFrom, dataItem)

		val prp = model.createResource()
		prp.addProperty(RDF.type, OWL.Restriction)
		prp.addProperty(OWL.onProperty, SPL.hasProcessing)
		if(makeIntersection)
			prp.addProperty(OWL.intersectionOf, createList(model, processing))
		else
			for(procItem in processing)
				prp.addProperty(OWL.someValuesFrom, procItem)

		val purp = model.createResource()
		purp.addProperty(RDF.type, OWL.Restriction)
		purp.addProperty(OWL.onProperty, SPL.hasPurpose)
		if(makeIntersection)
			purp.addProperty(OWL.intersectionOf, createList(model, purpose))
		else
			for(purpItem in purpose)
				purp.addProperty(OWL.someValuesFrom, purpItem)

		val rp = model.createResource()
		rp.addProperty(RDF.type, OWL.Restriction)
		rp.addProperty(OWL.onProperty, SPL.hasRecipient)
		if(makeIntersection)
			rp.addProperty(OWL.intersectionOf, createList(model, recipient))
		else
			for(recItem in recipient)
				rp.addProperty(OWL.someValuesFrom, recItem)

		val sp = model.createResource()
		sp.addProperty(RDF.type, OWL.Restriction)
		sp.addProperty(OWL.onProperty, SPL.hasStorage)
		if(makeIntersection)
			sp.addProperty(OWL.intersectionOf, createList(model, storage))
		else
			for(stItem in storage)
				sp.addProperty(OWL.someValuesFrom, stItem)

		val eqCls = model.createResource()
		eqCls.addProperty(RDF.type, OWL.Class)
		
		val rlist = createList(model, listOf(dp, prp, purp, rp, sp))
		eqCls.addProperty(OWL.intersectionOf, rlist)
		//for(item in parseList(rlist))
		//	println(item)

		val root = model.createResource(ResName)
		root.addProperty(RDF.type, OWL.Class)
		root.addProperty(OWL.equivalentClass, eqCls)

		//val tmp = model.createResource("_:empty")
		//tmp.addProperty(CPSS.refPolicy, RDF.nil)

		return model	
	}		
}

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
		val status = checkCompliance(refModelFn, userPolicyFn, userSubj, usagePolicyFn, usageSubj)
	}
}

fun createList(model:Model, res: List<Resource>):Resource {
	val list = model.createResource()
	var node = list

	var ctr = 0
	for(r in res){
		node.addProperty(RDF.first, r)
		if(ctr < res.size-1){
			val next = model.createResource()
			node.addProperty(RDF.rest, next)
			node = next
		}
		else node.addProperty(RDF.rest, RDF.nil)
		ctr++
	}

	return list
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

fun generateConfiguration(mashupConfigFilename: String, policyOutFilename: String):Model {
	/*
	load mashup configuration containing list of widgets and their wirings
	generate policies for mashup level
	*/
	val ds = RDFDataMgr.loadDataset(mashupConfigFilename)
	for(name in ds.listNames()) {
		println(name)
		val nm = ds.getNamedModel(name)
		println(nm.size())
		for(subj in nm.listSubjects())
			println(subj)
	}

	val model = ds.getUnionModel()
	println(model.size())

	val outModel = ModelFactory.createDefaultModel()

	val nn : RDFNode? = null // required for type-inference disambiguation

	val mashups = model.listResourcesWithProperty(RDF.type, LW.Mashup).toList()
	for(mashup in mashups){
		println(mashup)
		val mashupPolicyName = mashup.getURI()+"_Policy"
		val mashupPolicy = Policy(mashupPolicyName)

		val purposes = ArrayList<Resource>()
		for(stmt in model.listStatements(mashup, LW.hasPurpose, nn))
			purposes.add(stmt.getResource())


		val recipients = ArrayList<Resource>()
		for(stmt in model.listStatements(mashup, LW.hasRecipient, nn))
			recipients.add(stmt.getResource())

		val processes = ArrayList<Resource>()
		val storages = ArrayList<Resource>()
		val widgets = ArrayList<Resource>()
		for(stmt in model.listStatements(mashup, LW.hasWidget, nn)) {
			val widget = stmt.getResource()
			processes.add(widget.getProperty(LW.hasProcessing).getResource())
			storages.add(widget.getProperty(LW.hasStorage).getResource())
			widgets.add(widget)
		}

		val dataPayloads = ArrayList<Resource>()
		val wirings = model.listStatements(mashup, LW.hasWiring, nn)
		//val prev = HashMap<Resource,MutableList<Resource>>()
		//val next = HashMap<Resource,MutableList<Resource>>()
		for(wObj in wirings) {
			val wRes = wObj.getResource()
			val wOut = model.getProperty(wRes, LW.wiringOutput).getResource()
			val wIn = model.getProperty(wRes, LW.wiringInput).getResource()

			for(stmt in model.listStatements(wOut, LW.hasOutputData, nn))
				dataPayloads.add(stmt.getResource())

			print(wIn)
			print(" -> ")
			println(wOut)
		}

		// root are widgets which is in prev and not available in next
		//val roots = ArrayList<Resource>()
		//for( w in widgets)
		//	if(prev.containsKey(w) && !next.containsKey(w))
		//		roots.add(w)
		
		//start with root (outputWidget), recursively
		//for(w in roots)

		//TODO: if we want an intersection of values instead of separate statements
		val makeIntersection = false

		mashupPolicy.dataTypes.addAll(dataPayloads)
		mashupPolicy.storage.addAll(storages)	
		mashupPolicy.purpose.addAll(purposes)
		mashupPolicy.recipient.addAll(recipients)
		mashupPolicy.processing.addAll(processes)
		
		outModel.add(mashupPolicy.getModel(makeIntersection))
	}

	try {
		val os = FileOutputStream(policyOutFilename)
		RDFDataMgr.write(os, outModel, Lang.TURTLE)
		os.close()
	}
	catch(e:Exception) {

	}

	return outModel
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
	/*
	generateConfiguration("data\\mashup.ttl","data\\mashupPolicy.ttl")

	val policy = constructPolicy(LWR.get("Example_Policy").getURI(), 
		listOf(SVD.AnyData),
		listOf(SVPR.Transfer),
		listOf(SVPU.Develop),
		listOf(SVR.Ours),
		listOf(SVL.AT))
		*/
	//RDFDataMgr.write(System.out, policy, Lang.TURTLE) ;
	
	//testCompliance1()
	//testCompliance2()
	
	//println("total processing time (ms) : "+measureTimeMillis { checkFromConfig("data/config.ttl") })
	println("len args: "+args.size)
	if(args.size>0) println(args[0])
	if(args.size==2 && args[0] == "-check")
		println("total processing time (ms) : "+measureTimeMillis { checkFromConfig(args[1]) })
	else if(args.size==3 && args[0]== "-generate")
		generateConfiguration(args[1], args[2])
	else {
		println("Usage: privacycheck [-generate <mashup.ttl> <policy-out.ttl>] [-check <config.ttl>]")
		testCompliance1()//warming up loading refPolicyFile
		println("total processing time (ms) : "+measureTimeMillis { checkFromConfig("data/config1.ttl") })
		println("total processing time (ms) : "+measureTimeMillis { checkFromConfig("data/config2.ttl") })
	}
}