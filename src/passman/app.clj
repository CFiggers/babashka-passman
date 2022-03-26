(ns passman.app
  (:require [clojure.tools.cli :refer [parse-opts]]
            [passman.db :as db]
            [passman.password :refer [generate-password]]
            [passman.stash :as stash]
            [passman.clipboard :refer [copy]]
            [clojure.string :as str]
            [table.core :as t]))


(def cli-options
  [["-l" "--length Length" "Password length"
    :default 40
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-g" "--generate" "Generate new password"]
   [nil "--list"]
   ["-h" "--help" "Show help"]
   ["-u" "--usage" "Show usage"]
   ["-d" "--delete"]
   ["-p" "--password Password" "Manual password to add with given <url> and <username>."]
   ["-a" "--add" "Adds a manually provided password to the database."]
   ["-f" "--force-update" "Forces Add and Generate to overwrite existing."]])

(def usage
  (->> ["Passman CLI Password Manager v. 0.0.2-a"
        ""
        "Usage: passman <url> <username>"
        ""
        "Default behavior: Looks up password for the given <url> and <username>, copies to clipboard."
        ""
        "Available options:"
        "  [-h | --help] | [-u | --usage]        Show this help text. Ignores <url> and <username>."
        "  [-g | --generate]                     Generate a new password for given <url> and <username."
        "                                          Copies generated password to clipboard and saves to database."
        "  [-l <length> | --length <length>]     Specify length of password generated with [-g | --generate]. Defaults to 40."
        "  [-p <pass> | --password <pass>]       Provides a manual password to be stored with the given <url> and <username>."
        "                                          Ignored if not used with [-a | -add] or [-a! | -add!]."
        "  [-a | --add]                          Saves <pass> as a new password. Panics if <url> <username> combo already exists."
        "  [-d | --delete]                       Deletes the provided <url> and <username> combo. Must be passed with [-f | --force-update]."
        "  [-f | --force-update]                 Forces [-a | --add] and [-g | --generate] to overwrite existing <url> and <username> combo."
        "  [--list]                              Print table of all urls and usernames with stored passwords."
        "                                          Runs only if <url> and <username> are not supplied."]
       (str/join \newline)))

(defn password-input []
  (println "Enter your master key:")
  (String. (.readPassword (System/console))))

(defn confirm-overwrite []
  (print "Do you want to overwrite? [y/n] ") (flush)
  (let [input (str/lower-case (String. (.readLine (System/console))))]
    (case input "y" true false)))

(defn confirm-delete []
  (print "Are you sure you want to delete? (This cannot be undone) [yes/no] ") (flush)
  (let [input (str/lower-case (String. (.readLine (System/console))))]
    (case input "yes" true false)))

(defn delete-pass [url username]
  (stash/stash-init (password-input))
  (println (str "!!! DELETING: URL " url " and Username " username " !!!"))
  (let [confirm (confirm-delete)]
    (if confirm
      (do (db/delete-password! url username)
          (stash/delete-password! url username)
          (println "Deleted password"))
      (println "Canceled"))))

(defn add-pass [url username password overwrite]
  (stash/stash-init (password-input))
  (let [existing-pass (stash/find-password url username)]
    (if existing-pass
      (do (println "This url/username combo already exists!")
          (when overwrite
            (let [confirm (confirm-overwrite)]
              (if confirm
                (do (stash/update-password! url username password)
                    (println "Updated password, copied to clipboard")
                    (copy password))
                (println "Canceled")))))
      (do (db/insert-password! url username)
          (stash/insert-password! url username password)
          (println "Added password, copied to clipboard")
          (copy password)))))

(defn generate-new-pass [url username length overwrite]
  (let [password (generate-password length)]
    (add-pass url username password overwrite)))

(defn -main [& args]
  (let [parsed-options (parse-opts args cli-options)
        options (:options parsed-options)
        [url username] (:arguments parsed-options)]
    ;; (println options) ;; Uncomment to debug options
    (cond
      (or (:help options) (:usage options)) (println usage)
      (:delete options) (if (:force-update options)
                          (if (and url username)
                            (delete-pass url username)
                            (println "You must specify a url/username combo to delete."))
                          (println "You must pass [-f | --force-update] to delete a url/username combo."))
      (:generate options) (generate-new-pass url username (:length options) (:force-update options))
      (:add options) (if (:password options)
                       (add-pass url username (:password options) (:force-update options))
                       (println "Provide a [-p <pass> | --password <pass>] argument with [-a | --add]"))
      (and url username) (do
                           (stash/stash-init (password-input))
                           (let [password (stash/find-password url username)]
                             (if password
                               (do (copy password)
                                   (println "Password copied to clipboard"))
                               (println "Password not found for that url/username combo."))))
      (:list options) (t/table (db/list-passwords))
      :else (println usage))))

(comment
  (-main))
