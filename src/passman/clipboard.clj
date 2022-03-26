(ns passman.clipboard
  (:require [babashka.process :refer [sh]]))

(defn copy [text]
  (-> (sh ["echo" text])
      (sh ["clip.exe"]) :out))

(comment
  (copy "hello there"))