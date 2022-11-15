VERSION=$(shell util/version)

version-info: clean
	echo "{:pret/version \"$(VERSION)\"}" > resources/info.edn

cache-schema:
	clojure -M -m org.candelbio.pret.db.schema.cache

uberjar: version-info cache-schema
	clojure -Mdepstar target/pret.jar

package-prod: uberjar
	mkdir target/pret-$(VERSION)
	mkdir target/pret-$(VERSION)/env
	cp target/pret.jar target/pret-$(VERSION)/.
	cp pret target/pret-$(VERSION)/.
	cp pretw.bat target/pret-$(VERSION)/.
	cd target && \
		zip -r pret-$(VERSION).zip pret-$(VERSION)

clean:
	mkdir -p target
	rm -rf target/*
