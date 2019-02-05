package net.cityspin.privacy

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource

import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.OWL

import net.cityspin.vocab.CPSS
import net.cityspin.vocab.LW
import net.cityspin.vocab.LWR

import net.cityspin.vocab.special.SPL
import net.cityspin.vocab.special.SVD
import net.cityspin.vocab.special.SVPR
import net.cityspin.vocab.special.SVPU
import net.cityspin.vocab.special.SVR
import net.cityspin.vocab.special.SVL

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