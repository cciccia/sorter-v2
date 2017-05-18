(ns sorter.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [sorter.core-test]))

(doo-tests 'sorter.core-test)
