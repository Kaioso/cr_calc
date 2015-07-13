(ns cr-calc.core
  (:use seesaw.core))

(def cr-items
  {:d "Defense CR" :o "Offense CR" :a "Average CR"})

(def cr-labels-max-length
  (apply max (map #(.length %) (vals cr-items))))

(defn to-ranges [ranges]
  (into {} (apply concat (for [[start end bonus cr] ranges] (for [n (range start (+ 1 end))] [n [cr bonus]])))))

(def crs
  (into {}
        (concat [[1 0]
                 [2 (/ 1 8)]
                 [3 (/ 1 4)]
                 [4 (/ 1 2)]]
                (map vector (iterate inc 5) (range 1 30)))))

(def defense-cr-value-table
  (let [crs-d [[1 6 11]
               [7 35 13]
               [36 49 13]
               [50 70 13]
               [71 85 13]
               [86 100 13]
               [101 115 13]
               [116 130 14]
               [131 145 15]
               [146 160 15]
               [161 175 15]
               [176 190 16]
               [191 205 16]
               [206 220 17]
               [221 235 17]
               [236 250 17]
               [251 265 18]
               [266 280 18]
               [281 295 18]
               [296 310 18]
               [311 325 19]
               [326 340 19]
               [341 355 19]
               [356 400 19]
               [401 445 19]
               [446 490 19]
               [491 535 19]
               [536 580 19]
               [581 625 19]
               [626 670 19]
               [671 715 19]
               [716 760 19]
               [761 805 19]
               [806 850 19]]]
    (to-ranges (map #(conj %1 %2) crs-d (iterate inc 1)))))

(def offense-cr-value-table
  (let [crs-o [[0 1 1]
               [2 3 3]
               [4 5 3]
               [6 8 3]
               [9 14 3]
               [15 20 3]
               [21 26 4]
               [27 32 5]
               [33 38 6]
               [39 44 6]
               [45 50 6]
               [51 56 7]
               [57 62 7]
               [63 68 7]
               [69 74 8]
               [75 80 8]
               [81 86 8]
               [87 92 8]
               [93 98 8]
               [99 104 9]
               [105 110 10]
               [111 116 10]
               [117 122 10]
               [123 140 10]
               [141 158 11]
               [159 176 11]
               [177 194 11]
               [195 212 12]
               [213 230 12]
               [231 248 12]
               [249 266 13]
               [267 284 13]
               [285 302 13]
               [303 320 14]]]
    (to-ranges (map #(conj %1 %2) crs-o (iterate inc 1)))))

(defn to-defense-cr [hp ac]
  (if-let [[cr-lookup cr-ac] (get defense-cr-value-table hp)]
    (let [ac-diff (- ac cr-ac)
          actual-lookup (let [diff (Math/round (Math/floor (/ (Math/abs ac-diff) 2)))]
                          (if (< ac-diff 0)
                            (max 0 (- cr-lookup diff))
                            (min 30 (+ cr-lookup diff))))]
      (get crs actual-lookup))))

(defn to-offense-cr [damage atk]
  (if-let [[cr-lookup cr-atk] (get offense-cr-value-table damage)]
    (let [atk-diff (- atk cr-atk)
          actual-lookup (let [diff (Math/round (Math/floor (/ (Math/abs atk-diff) 2)))]
                          (if (< atk-diff 0)
                            (max 0 (- cr-lookup diff))
                            (min 30 (+ cr-lookup diff))))]
      (get crs actual-lookup))))

(defn cr-average [d-cr o-cr]
  (cond
   (== 0 d-cr) o-cr
   (== 0 o-cr) d-cr
   :else
   (let [average (/ (+ d-cr o-cr) 2)
         sorted-crs (into [] (sort (vals crs)))]
     (when-let [next-highest-index (first (keep-indexed #(when (> %2 average) %1) sorted-crs))]
       (let [low (or (get sorted-crs (- next-highest-index 1))
                     0)
             high (get sorted-crs next-highest-index)
             low-diff (Math/abs (float (- average low)))
             high-diff (Math/abs (float (- average high)))]
         (cond
          (== 0 low-diff) low
          (== 0 high-diff) high
          (> low-diff high-diff) low
          :else high))))))

(defn to-int [int-str]
  (try
    (Integer/parseInt int-str)
    (catch NumberFormatException e 0)))

(defn -main [& args]
  (let [cr-labels (into {}
                        (for [[k s] cr-items]
                          [k (label :text
                                    (str s
                                         (apply str (repeat (max (- cr-labels-max-length (.length s))
                                                                 0)
                                                            " "))
                                         ": ")
                                                           :font :monospaced)]))
        inputs (for [s ["HP " "AC " "Atk" "Dmg"]]
                 (let [txt (text)]
                   [(keyword (.trim s)) txt (horizontal-panel :items [(label :text (str s ":") :font :monospaced) txt])]))
        stats (into {} (map #(into [] (take 2 %)) inputs))
        main-frame (frame :title "CR Calc"
                          :on-close :exit
                          :content (vertical-panel :items (concat (vals cr-labels) (map last inputs)))
                          :size [640 :by 480])]
    (do
      (doseq [[k txt _] inputs]
        (listen txt
                :document (fn [e] (let [d (to-defense-cr (to-int (config (:HP stats) :text))
                                                              (to-int (config (:AC stats) :text)))
                                       o (to-offense-cr (to-int (config (:Dmg stats) :text))
                                                              (to-int (config (:Atk stats) :text)))
                                       fin {:d d :o o :a (cr-average (or d 0) (or o 0))}]
                                   (doseq [k [:d :o :a]]
                                     (config! (k cr-labels) :text (str (k cr-items)
                                                                       (apply str (repeat (max (- cr-labels-max-length (.length (k cr-items)))
                                                                                               0)
                                                                                          " "))
                                                                       ": "
                                                                       (k fin))))))))
      (-> main-frame
          ;;        pack!
          show!))))
