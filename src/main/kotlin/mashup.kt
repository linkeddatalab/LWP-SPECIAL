package net.cityspin.mashup

import java.io.FileOutputStream

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.RDFNode

import openllet.jena.PelletReasonerFactory
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.Lang

import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.OWL

import net.cityspin.vocab.CPSS
import net.cityspin.vocab.LW

import net.cityspin.privacy.Policy

fun generateConfiguration(mashupConfigFilename: String, policyOutFilename: String):Model {
	/*
	load mashup configuration containing list of widgets and their wirings
	generate policies for mashup level
	*/
	var model: Model
	if(mashupConfigFilename.endsWith(".trig")) {
		println(mashupConfigFilename)
		val ds = RDFDataMgr.loadDataset(mashupConfigFilename)
		for(name in ds.listNames()) {
			println(name)
			val nm = ds.getNamedModel(name)
			println(nm.size())
			for(subj in nm.listSubjects())
				println(subj)
		}

		model = ds.getUnionModel()
		println(model.size())
	}
	else {
		model = RDFDataMgr.loadModel(mashupConfigFilename)
	}

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
		for(wObj in wirings) {
			val wRes = wObj.getResource()
			val wOut = model.getProperty(wRes, LW.wiringOutput).getResource()
			val wIn = model.getProperty(wRes, LW.wiringInput).getResource()

			for(stmt in model.listStatements(wIn, LW.hasInputData, nn))
				dataPayloads.add(stmt.getResource())

			print(wIn)
			print(" -> ")
			println(wOut)
		}

		//filter out data objects that are subclass of another object
		val _model = ModelFactory.createDefaultModel()
		RDFDataMgr.read(_model, "cpss-special-integrated.ttl")
		val _reasoner = PelletReasonerFactory.theInstance().create()
		val _eModel = ModelFactory.createInfModel(_reasoner, _model)
		val toRemove = ArrayList<Resource>()
		for(dob1 in dataPayloads)
			for(dob2 in dataPayloads)
				if (dob1 != dob2) {
						if(_eModel.contains(dob1, RDFS.subClassOf, dob2))
							toRemove.add(dob1)
						else if(_eModel.contains(dob2, RDFS.subClassOf, dob1))
							toRemove.add(dob2)
					}
		if(toRemove.size>0)
			dataPayloads.removeAll(toRemove)	

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