```
         8I               
         8I               
         8I               
         8I               
   ,gggg,8I     ,gg,   ,gg
  dP"  "Y8I    d8""8b,dP" 
 i8'    ,8I   dP   ,88"   
,d8,   ,d8b,,dP  ,dP"Y8,  
P"Y8888P"`Y88"  dP"   "Y88
```               

# definition execution

Third iteration: state is stored in agents, minimal deps.

## abbreviations

* `cont` ... container 
* `defis` ... definitions
* `ndx` ... container number
* `idx` ... sequential step
* `jdx` ... parallel step
* `exch` ... exchange interface

## mem

## mpd

```clojure
  ;; generate a fresh mpd
  (def m (-> {}
          mpd/standard->
          mpd/name->
          mpd/descr->
          mpd/exch->
          mpd/cont->
          mpd/defi->))
```

`m`looks like this:

```clojure
{:Mp
 {:Standard "NN",
  :Name "generic",
  :Description "Default description",
  :Exchange {:Default {:Bool true}},
  :Container
  [{:Title "Default title",
    :Description "Default container description",
    :Element [:Default],
    :Definition [[{:TaskName "Common-wait"}]]}],
  :Definitions
  [{:DefinitionClass "default",
    :ShortDescr "Default description",
    :Condition
    [{:ExchangePath "Default.Bool", :Methode "eq", :Value true}],
    :Definition [[{:TaskName "Common-wait"}]]}]},
 :_id "mpd-nn-generic"}
 ```

```clojure
(def mem (atom {}))
(up m)
(deref mem)
```
`mem` looks loke this:

```clojure
{:mpd-nn-generic
 {:Standard "NN",
  :Name "generic",
  :Description "Default description",
  :Exchange #<Agent@4c66db73: nil>,
  :Container
  [{:Title "Default title",
    :Description "Default container description",
    :Element [:Default],
    :Definition [[{:TaskName "Common-wait"}]],
    :State
    #<Agent@4937c4d3: 
      {:states
       [{:mp-id :mpd-nn-generic,
         :struct :Container,
         :ndx 0,
         :idx 0,
         :jdx 0,
         :state :ready}],
       :ctrl :ready}>,
    :Future #<Future@9669e3c: :pending>}],
  :Definitions
  [{:DefinitionClass "default",
    :ShortDescr "Default description",
    :Condition
    [{:ExchangePath "Default.Bool", :Methode "eq", :Value true}],
    :Definition [[{:TaskName "Common-wait"}]],
    :State
    #<Agent@91f1b42: 
      {:states
       [{:mp-id :mpd-nn-generic,
         :struct :Definitions,
         :ndx 0,
         :idx 0,
         :jdx 0,
         :state :ready}],
       :ctrl :ready}>,
    :Future #<Future@791e4d85: :pending>}]}}
```

## testing

```shell
clj -M:test
```
