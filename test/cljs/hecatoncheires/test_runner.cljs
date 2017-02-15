(ns hecatoncheires.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [hecatoncheires.core-test]
   [hecatoncheires.common-test]))

(enable-console-print!)

(doo-tests 'hecatoncheires.core-test
           'hecatoncheires.common-test)
