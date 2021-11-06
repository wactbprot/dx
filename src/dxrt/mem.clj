(ns dxrt.mem)

(def mem {})

(defn add-struct [v m] (assoc-in mem v (agent m))) 
