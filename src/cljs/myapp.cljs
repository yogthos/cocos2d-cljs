(ns myapp.game
  (:require [clojure.browser.repl :as repl]))

(defn layer-ctor []
  (this-as this
    (.associateWithNative js/cc this (.-Layer js/cc))))

(defn scene-ctor []
  (this-as this
    (.associateWithNative js/cc this (.-Scene js/cc))))

(def space (atom (cp/Space.)))

(defn init-fn []
  (repl/connect "http://localhost:9000/repl")

  (this-as this
    (._super this)
    (let [size (-> cc/Director .getInstance .getWinSize)]
      (set! (.-sprite this) (.create cc/PhysicsSprite "watermelon.png"))
      (set! (.-body this) (cp/Body. 1 (.momentForBox js/cp 1 48 48)))
      (.setBody (.-sprite this) (.-body this))
      (.addBody @space (.-body this))
      (let [shape (cp/BoxShape. (.-body this) 64 64)]
        (.setElasticity shape 1)
        (.setFriction shape 1)
        (.addShape @space shape))

      (doto (.-sprite this)
        (.setPosition (.p js/cc (/ (.-width size) 2) (/ (.-height size) 2)))
        (.setVisible true)
        (.setAnchorPoint (.p js/cc 0.5 0.5))
        (.setScale 0.5)
        (.setRotation 90))
      (.addChild this (.-sprite this) 0)
      (.setTouchEnabled this true)


      (set! (.-scoreLabel this) (.create cc/LabelTTF "0" "Arial" 32))
      (doto (.-scoreLabel this)
        (.setAnchorPoint (.p js/cc 0 0))
        (.setPosition (.p js/cc 130 (- (.-height size) 48)))
        (.setHorizontalAlignment (.-TEXT_ALIGNMENT_LEFT js/cc)))

      (.addChild this (.-scoreLabel this)))))


(def clicks (atom 0))


(defn on-touches-began [touches events]
  (this-as this
    (let [sprite (.-sprite this)
          current-rotation (.getRotation sprite)]
      (swap! clicks inc)
      (.setString (.-scoreLabel this) @clicks)
      (.setRotation sprite (+ current-rotation 5)))))

(def params (js-obj js/isMouseDown false
                    js/helloImg nil
                    js/helloLb nil
                    js/circle nil
                    js/sprite nil
                    js/scoreLabel nil
                    js/init init-fn
                    js/ctor layer-ctor
                    js/onTouchesBegan on-touches-began))

(def ^:export hello-world-layer (.extend cc/Layer params))

(defn update [delta]
  (.step @space delta))

(def scene-params (js-obj js/ctor scene-ctor
                   js/space nil
                   js/update update
                   js/onEnter (fn []
  (this-as this
    (._super this)

    (let [wall (cp/SegmentShape. (.-staticBody @space) (.v js/cp 0 0) (.v js/cp 800 0) 0)]
      (.setElasticity wall 1)
      (.setFriction wall 1)
      (.addStaticShape @space wall))

    (set! (.-gravity @space) (.v js/cp 0 -100))
    (.scheduleUpdate this)
    (let [layer (hello-world-layer.)]
      (.init layer)
      (.addChild this layer))))))

(def ^:export hello-world-scene (.extend cc/Scene scene-params))
