(ns bosquet.openai
  (:require
   [clojure.string :as string]
   [wkok.openai-clojure.api :as api]))

(def ada "text-ada-001")

#_:clj-kondo/ignore
(def davinci "text-davinci-003")

#_:clj-kondo/ignore
(def cgpt "gpt-3.5-turbo")


(defn- create-chat
  "Completion using Chat GPT model. This one is loosing the conversation
  aspect of the API. It will construct basic `system` for the
  conversation and then use `prompt` as the `user` in the chat "
  [prompt params opts]
  (-> (api/create-chat-completion
        (assoc params
          :model cgpt
          :messages [{:role "system" :content "You are a helpful assistant."}
                     {:role "user" :content prompt}])
        opts)
    :choices first :message :content))

(defn- create-completion
  "Create completion (not chat) for `prompt` based on model `params` and invocation `opts`"
  [prompt params opts]
  (-> (api/create-completion
        (assoc params :prompt prompt) opts)
    :choices first :text))

(defn complete
  "Complete `prompt` if passed in `model` is cGPT the completion will
  be passed to `complete-chat`"
  ([prompt] (complete prompt nil))
  ([prompt {:keys [impl api-key
                   api-endpoint
                   model temperature max-tokens n top-p
                   presence-penalty frequence-penalty]
            :or   {impl              :openai
                   model             ada
                   temperature       0.6
                   max-tokens        250
                   presence-penalty  0.4
                   frequence-penalty 0.2
                   top-p             1
                   n                 1}}]
   (let [params {
                 :model             model
                 :temperature       temperature
                 :max_tokens        max-tokens
                 :presence_penalty  presence-penalty
                 :frequency_penalty frequence-penalty
                 :n                 n
                 :top_p             top-p
                 :prompt            prompt}
         opts   {:api-key           api-key
                 :api-endpoint      api-endpoint
                 :impl              (keyword impl)}]
     (spit "/tmp/prompt.txt" prompt)
     (if (= model cgpt)
       (create-chat prompt params opts)
       (create-completion prompt params opts)))))



(comment
  (complete "What is your name?" {:max-tokens 10 :model cgpt})
  (complete "What is your name?" {:max-tokens 10})
  (complete "1 + 10 =" {:model "testtextdavanci003" :impl :azure}))
