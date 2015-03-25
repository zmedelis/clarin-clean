(ns clarin-cleanup.core-test
  (:require [midje.sweet :refer :all]
            [clarin-cleanup.core :refer :all]))

(fact "Get title line"
  (title? "ddddd") => falsey
  (title? nil) => falsey
  (title? "") => falsey
  (title? " dc:title") => truthy
  (title? "\t\ndc:title") => truthy)

(fact "Get text line"
  (text? "ddddd") => falsey
  (text? "lpv:text") => truthy)

(fact "See how we get ontology assigned language"
  (onto-assigned-language "xxxxx\"@hr ,") => "hr"
  (onto-assigned-language "xxxxx\"    @hr,") => "hr"
  (onto-assigned-language "xxxxx\"    @hr ;") => "hr"
  (onto-assigned-language "xxxxx\"    @hr.") => "hr"
  (onto-assigned-language "xxxxx\"@hr,") => "hr"
  (onto-assigned-language "xxxxx\"hr,") => nil
  (onto-assigned-language "xxxxx@hr,") => nil)

(fact "Fix wrong language attribtion"
  (fix-language "\"Podnošenje dokumenata: vidi zapisnik\"@en .")
  => "\"Podnošenje dokumenata: vidi zapisnik\"@hr .")

(fact "Check if langiage change is allowed"
  (can-fix? "en" ["en" 0.4]) => false
  (can-fix? "en" ["lt" 0.4]) => false
  (can-fix? "en" ["lt" 0.9]) => true)

(fact "Clean junk from text lines"
  (remove-junk "dc:title \"aaa\"@lt .") => "dc:title \"aaa\"@lt ."
  (remove-junk "\tdc:title \"aaa\"@lt .") => "\tdc:title \"aaa\"@lt ."
  (remove-junk "dc:title \" aaa    \"@lt .") => "dc:title \"aaa\"@lt ."
  (remove-junk "dc:title \"\n\n  \n  aaa  \t\t\t\n\n\n\"@lt .") =>"dc:title \"aaa\"@lt ."
  (remove-junk "dc:title \"\n\n  \n    –(FR) I agree\"@lt .") => "dc:title \"I agree\"@lt ."
  (remove-junk "dc:title \"\n\n  \n    –(FR) I agree    \"@lt .") => "dc:title \"I agree\"@lt ."
  (remove-junk "dc:title \"\n\n- Rapport: Laperrouze ()\"@lt .") => "dc:title \"Rapport: Laperrouze\"@lt ."

  (remove-junk
    "\tlpv:text \"\n\n  \n I declare resumed the session of the European Parliament adjourned on Thursday, 22 April 2004.\"@en ;")
    =>
    "\tlpv:text \"I declare resumed the session of the European Parliament adjourned on Thursday, 22 April 2004.\"@en ;"

    )

