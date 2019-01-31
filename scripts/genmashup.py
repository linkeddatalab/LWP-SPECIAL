from rdflib import Graph, RDF, RDFS, OWL, Namespace, URIRef, BNode, Literal
import os
import sys
import random
import argparse

from vocab import *

LW = Namespace("http://linkedwidgets.org/ns/ontology#")
LWR = Namespace("http://linkedwidgets.org/ns/resource/")
CPSS = Namespace("http://w3id.org/cityspin/ns/special-cpss#")

widget_types = [LW["InputWidget"], LW["ProcessingWidget"], LW["OutputWidget"]]
data_types = [CPSS["UserLocation"], CPSS["EnergyConsumption"], CPSS["UserInfo"], CPSS["AnonymizedUserInfo"]]

def select_random(l):
	return random.choice(l)
	#return l[random.randint(0, len(l)-1)]

def create_random_widget(g, widgetType=None):
	widget_name = "{}{}Widget".format("Random", random.randint(0,65536))
	widget_uri = LWR[widget_name]
	widget_type = select_random(widget_types) if widgetType is None else widgetType
	g.add((widget_uri, RDF.type, widget_type))
	g.add((widget_uri, RDFS.label, Literal(widget_name)))
	if widget_type in widget_types[1:]:
		g.add((widget_uri, LW["hasInputData"], select_random(data_types)))
	if widget_type in widget_types[:2]:
		g.add((widget_uri, LW["hasOutputData"], select_random(data_types)))
	return widget_uri

def compose_mashup(g, widgetlist):
	mashup_name = "{}{}Mashup".format("Random", random.randint(0,65536))
	mashup_uri  = LWR[mashup_name]
	g.add((mashup_uri, RDF.type, LW["Mashup"]))
	for w in widgetlist:
		g.add((mashup_uri, LW["hasWidget"], w))
	return mashup_uri

if __name__=="__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument('-numWidgets', type=int, default=3, help="Number of Widget in the mashup, min: 2")
	args = parser.parse_args()

	if args.numWidgets < 2:
		print("at least two widgets required to make a mashup")
		exit(1)

	g = Graph()
	bindNS(g)

	num_widgets = args.numWidgets
	widgets = []

	widgets.append(create_random_widget(g, LW["OutputWidget"]))
	widgets.append(create_random_widget(g, LW["InputWidget"]))
	for wi in range(num_widgets-2):
		widgets.append(create_random_widget(g, LW["ProcessingWidget"]))

	compose_mashup(g, widgets)	

	print(g.serialize(format="turtle").decode("latin-1"))
