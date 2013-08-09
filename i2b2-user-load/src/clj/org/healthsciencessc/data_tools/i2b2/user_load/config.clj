(ns org.healthsciencessc.data-tools.i2b2.user-load.config
  (:require [pliant.configure.props :refer [slurp-config]]))

(def config (slurp-config "i2b2-user-load.props"))
