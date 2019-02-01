from rdflib import Namespace

SPL = Namespace("https://www.specialprivacy.eu/langs/usage-policy#")
SVD = Namespace("http://www.specialprivacy.eu/vocabs/data#")
SVDU = Namespace("http://www.specialprivacy.eu/vocabs/duration#")
SVL = Namespace("https://www.specialprivacy.eu/vocabs/locations#")
SVR = Namespace("https://www.specialprivacy.eu/vocabs/recipients#")
SVPR = Namespace("http://www.specialprivacy.eu/vocabs/processing#")
SVPU = Namespace("http://www.specialprivacy.eu/vocabs/purposes#")
LW = Namespace("http://linkedwidgets.org/ns/ontology#")
LWR = Namespace("http://linkedwidgets.org/ns/resource/")
CPSS = Namespace("http://w3id.org/cityspin/ns/special-cpss#")

def bindNS(g):
	g.bind("spl", SPL[""])
	g.bind("svd", SVD[""])
	g.bind("svr", SVR[""])
	g.bind("svdu", SVDU[""])
	g.bind("svl", SVL[""])
	g.bind("svpr", SVPR[""])
	g.bind("svpu", SVPU[""])
	g.bind("lw", LW[""])
	g.bind("lwr", LWR[""])
	g.bind("cpss", CPSS[""])