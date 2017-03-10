(ns hecatoncheires.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as string]))

;; -----------------------------------------------------------------------------
;; Utils
;; -----------------------------------------------------------------------------

;; Tooltips --------------------------------------------------------------------

(defn add-tooltips! [component]
  (let [node (dom/node component)
        tooltips (js/$ "[data-toggle=\"tooltip\"]")]
    (.tooltip tooltips)))

(defn remove-tooltips! [component]
  (let [node (dom/node component)
        tooltips (js/$ "[data-toggle=\"tooltip\"]")]
    (.tooltip tooltips "destroy")))

;; Temp ids --------------------------------------------------------------------

(let [id (atom 0)]
  (defn get-temp-id []
    (str "tempid-" (swap! id inc))))


;; -----------------------------------------------------------------------------
;; User
;; -----------------------------------------------------------------------------

(defui UserItem
  static om/Ident
  (ident [this {:keys [:db/id]}]
         [:db/id id])
  static om/IQuery
  (query [this]
         [:db/id :hec.user/name :hec.user/avatar])
  Object
  (render [this]
          (let [{:keys [:db/id
                        :hec.user/name
                        :hec.user/avatar
                        click-fn] :as props} (om/props this)]
            (html [:li
                   [:button.hec-user.btn.btn-default
                    {:type "button"
                     :style {:background-image (when avatar (str "url(" (.-rep avatar) ")"))}
                     :dangerouslySetInnerHTML {:__html "&nbsp"}
                     :on-click #(when click-fn (click-fn props))
                     :data-toggle "tooltip"
                     :data-placement "bottom"
                     :data-animation "false"
                     :title name}]])))
  (componentDidMount [this]
                     (add-tooltips! this))
  (componentWillUnmount [this]
                        (remove-tooltips! this)))

(def user-item (om/factory UserItem))

(defui UserList
  Object
  (render [this]
          (let [{:keys [users click-fn]} (om/props this)
                users (->> users
                           (map #(assoc % :click-fn click-fn))
                           (sort-by :hec.user/name))]
            (html [:ul.list-inline
                   (map user-item users)]))))

(def user-list (om/factory UserList))

(defui UserPicker
  Object
  (render [this]
          (let [{:keys [click-fn] :as users} (om/props this)
                users (assoc users :click-fn click-fn)]
            (html [:div.btn-group
                   [:button.hec-user.btn.btn-default.dropdown-toggle.fa.fa-plus
                    {:type "button"
                     :data-toggle "dropdown"
                     }]
                   [:div.dropdown-menu
                    {:style {:width "300px"
                             :padding-left "15px"
                             :padding-right "15px"}}
                    (user-list users)]]))))

(def user-picker (om/factory UserPicker))


;; -----------------------------------------------------------------------------
;; Repo
;; -----------------------------------------------------------------------------

(defui RepoItem
  static om/Ident
  (ident [this {:keys [:db/id]}]
         [:db/id id])
  static om/IQuery
  (query [this]
         [:db/id :gh.repo/name])
  Object
  (render [this]
          (let [{:keys [:gh.repo/name
                        :click-fn] :as props} (om/props this)]
            (html [:li
                   [:button.btn.btn-default.btn-sm
                    {:type "button"
                     :style {:margin-bottom "6px"}
                     :on-click #(when click-fn (click-fn props))}
                    [:span.fa.fa-github]
                    (str " " name)]]))))

(def repo-item (om/factory RepoItem))

(defui RepoList
  Object
  (render [this]
          (let [{:keys [repos click-fn]} (om/props this)
                repos (->> repos
                           (map #(assoc % :click-fn click-fn))
                           (sort-by :gh.repo/name))]
            (html [:ul.list-inline
                   (map repo-item repos)]))))

(def repo-list (om/factory RepoList))

(defui RepoPicker
  Object
  (render [this]
          (let [{:keys [click-fn name-filter] :as repos} (om/props this)
                {:keys [f]} (om/get-state this)
                f (or f name-filter "")
                repos (-> repos
                          (assoc :click-fn click-fn)
                          (update-in [:repos]
                                     (fn [r]
                                       (filter
                                        #(-> % :gh.repo/name string/lower-case
                                             (string/includes? (string/lower-case f)))
                                        r))))]
            (html [:div.btn-group
                   [:button.btn.btn-default.btn-sm.dropdown-toggle
                    {:type "button"
                     :style {:margin-bottom "6px"}
                     :data-toggle "dropdown"}
                    [:span.fa.fa-plus]]
                   [:div.dropdown-menu
                    {:style {:width "400px"}}
                    [:div
                     {:style {:padding "10px"
                              :padding-left "15px"
                              :padding-right "15px"}}
                     [:div.input-group
                      [:span.input-group-addon
                       [:span.fa.fa-github]]
                      [:input.form-control
                       {:type "text" :value f
                        :on-change #(om/update-state! this assoc
                                                      :f
                                                      (.. % -target -value))}]]]

                    [:div.divider]
                    [:div
                     {:style {:padding "10px"
                              :padding-left "15px"
                              :padding-right "15px"}}
                     (repo-list repos)]]]))))

(def repo-picker (om/factory RepoPicker))


;; -----------------------------------------------------------------------------
;; Stack
;; -----------------------------------------------------------------------------

(defui StackItem
  static om/Ident
  (ident [this {:keys [:db/id]}]
         [:db/id id])
  static om/IQuery
  (query [this]
         [:db/id :aws.cf.stack/name])
  Object
  (render [this]
          (let [{:keys [:aws.cf.stack/name
                        :click-fn] :as props} (om/props this)]
            (html [:li
                   [:button.btn.btn-default.btn-sm
                    {:type "button"
                     :style {:margin-bottom "6px"}
                     :on-click #(when click-fn (click-fn props))}
                    [:span.fa.fa-list]
                    (str " " name)]]))))

(def stack-item (om/factory StackItem))

(defui StackList
  Object
  (render [this]
          (let [{:keys [stacks click-fn]} (om/props this)
                stacks (->> stacks
                            (map #(assoc % :click-fn click-fn))
                            (sort-by :aws.cf.stack/name))]
            (html [:ul.list-inline
                   (map stack-item stacks)]))))

(def stack-list (om/factory StackList))

(defui StackPicker
  Object
  (render [this]
          (let [{:keys [click-fn name-filter] :as stacks} (om/props this)
                {:keys [f]} (om/get-state this)
                f (or f name-filter "")
                stacks (-> stacks
                           (assoc :click-fn click-fn)
                           (update-in [:stacks]
                                      (fn [r]
                                        (filter
                                         #(-> % :aws.cf.stack/name string/lower-case
                                              (string/includes? (string/lower-case f)))
                                         r))))]
            (html [:div.btn-group
                   [:button.btn.btn-default.btn-sm.dropdown-toggle
                    {:type "button"
                     :style {:margin-bottom "6px"}
                     :data-toggle "dropdown"}
                    [:span.fa.fa-plus]]
                   [:div.dropdown-menu
                    {:style {:width "400px"}}
                    [:div
                     {:style {:padding "10px"
                              :padding-left "15px"
                              :padding-right "15px"}}
                     [:div.input-group
                      [:span.input-group-addon
                       [:span.fa.fa-list]]
                      [:input.form-control
                       {:type "text" :value f
                        :on-change #(om/update-state! this assoc
                                                      :f
                                                      (.. % -target -value))}]]]

                    [:div.divider]
                    [:div
                     {:style {:padding "10px"
                              :padding-left "15px"
                              :padding-right "15px"}}
                     (stack-list stacks)]]]))))

(def stack-picker (om/factory StackPicker))


;; -----------------------------------------------------------------------------
;; Components
;; -----------------------------------------------------------------------------

(defui ComponentItem
  static om/Ident
  (ident [this {:keys [:db/id]}]
         [:db/id id])
  static om/IQuery
  (query [this]
         (let [user-q (om/get-query UserItem)
               repo-q (om/get-query RepoItem)
               stack-q (om/get-query StackItem)]
           `[:db/id :hec.component/name
             {:hec.component/leader ~user-q}
             {:hec.component/repo ~repo-q}
             {:hec.component/stack ~stack-q}]))
  Object
  (render [this]
          (let [{:keys [:db/id
                        :hec.component/name
                        :hec.component/leader
                        :hec.component/repo
                        :hec.component/stack] :as props} (om/props this)]
            (html [:div.col-md-12
                   [:div.panel.panel-default
                    [:div.panel-body
                     [:div.row
                      [:div.col-md-2
                       [:h4.panel-title name]
                       [:p.text-muted id]]
                      [:div.col-md-1 (user-list {:users leader})]
                      [:div.col-md-4 (repo-list {:repos repo})]
                      [:div.col-md-4 (stack-list {:stacks stack})]]]]]))))

(def component-item (om/factory ComponentItem))

(defui ComponentList
  Object
  (render [this]
          (let [list (om/props this)
                list (sort-by :hec.component/name list)]
            (html [:div.row
                   (map component-item list)]))))

(def component-list (om/factory ComponentList))

(def init-form
  {:hec.component/name ""
   :hec.component/leader #{}
   :hec.component/repo #{}
   :hec.component/stack #{}})

(defn state->component
  [{:keys [name leader repo stack]}]
  {:hec.component/name name
   :hec.component/leader (reduce #(conj %1 [:db/id %2])  [] leader)
   :hec.component/repo (reduce #(conj %1 [:db/id %2])  [] repo)
   :hec.component/stack (reduce #(conj %1 [:db/id %2])  [] stack)})

(defui ComponentForm
  Object
  (componentWillMount [this]
                      (om/set-state! this init-form))
  (render [this]
          (let [{:keys [:hec.component/name :hec.component/leader :hec.component/repo :hec.component/stack] :as component} (om/get-state this)
                {:keys [users repos stacks]} (om/props this)
                leaders (->> users
                             (filter #(-> % :db/id leader))
                             (sort-by :hec.user/name))
                repositories (->> repos
                                  (filter #(-> % :db/id repo))
                                  (sort-by :gh.repo/name))
                aws-stacks (->> stacks
                                (filter #(-> % :db/id stack))
                                (sort-by :aws.cf.stack/name))]
            (html [:form
                   [:div.form-group
                    [:label "Name"]
                    [:input.form-control
                     {:type "text" :name "Name" :value name
                      :on-change #(om/update-state! this assoc
                                                    :hec.component/name
                                                    (.. % -target -value))}]]
                   [:div.form-group
                    [:label "Leaders"]
                    [:ul.list-inline
                     (map #(user-item (assoc %
                                             :click-fn
                                             (fn [{:keys [:db/id]}]
                                               (om/update-state! this update-in
                                                                 [:hec.component/leader]
                                                                 disj id))))
                          leaders)
                     [:li
                      (user-picker {:users users
                                    :click-fn (fn [{:keys [:db/id]}]
                                                (om/update-state! this update-in
                                                                  [:hec.component/leader]
                                                                  conj id))})]]]
                   [:div.form-group
                    [:label "Repositories"]
                    [:ul.list-inline
                     (map #(repo-item (assoc %
                                             :click-fn
                                             (fn [{:keys [:db/id]}]
                                               (om/update-state! this update-in
                                                                 [:hec.component/repo]
                                                                 disj id))))
                          repositories)
                     [:li.list-inline-item
                      (repo-picker {:repos repos
                                    :name-filter name
                                    :click-fn (fn [{:keys [:db/id]}]
                                                (om/update-state! this update-in
                                                                  [:hec.component/repo]
                                                                  conj id))})]]]
                   [:div.form-group
                    [:label "Stacks"]
                    [:ul.list-inline
                     (map #(stack-item (assoc %
                                              :click-fn
                                              (fn [{:keys [:db/id]}]
                                                (om/update-state! this update-in
                                                                  [:hec.component/stack]
                                                                  disj id))))
                          aws-stacks)
                     [:li.list-inline-item
                      (stack-picker {:stacks stacks
                                     :name-filter name
                                     :click-fn (fn [{:keys [:db/id]}]
                                                 (om/update-state! this update-in
                                                                   [:hec.component/stack]
                                                                   conj id))})]]]
                   [:div.btn.btn-primary
                    {:data-dismiss "modal"
                     :on-click #(let [reconciler (om/get-reconciler this)
                                      component (assoc component :db/id (get-temp-id))
                                      ]
                                  (om/transact! reconciler
                                                `[(component/create
                                                   ~component)])
                                  (om/react-set-state! this init-form)
                                  )}
                    "Create"]]))))

(def component-form (om/factory ComponentForm))

(defui ComponentFormModal
  Object
  (render [this]
          (let [{:keys [modal-id users] :as props} (om/props this)]
            (html [:div.modal.fade {:id modal-id
                                    :tabIndex "-1"
                                    :role "dialog"
                                    :aria-labelledby "componentCreation"
                                    :aria-hidden "true"}
                   [:div.modal-dialog.modal-lg {:role "document"}
                    [:div.modal-content
                     [:div.modal-header
                      [:button.close {:type "button"
                                      :data-dismiss "modal"
                                      :aria-label "Close"}
                       [:span {:aria-hidden "true"
                               :dangerouslySetInnerHTML {:__html "&times;"}}]]
                      [:h4.modal-title "New component"]]
                     [:div.modal-body
                      (component-form props)]]]]))))

(def component-form-modal (om/factory ComponentFormModal))


;; -----------------------------------------------------------------------------
;; App
;; -----------------------------------------------------------------------------

(defui App
  static om/IQuery
  (query [this]
         (let [user-q (om/get-query UserItem)
               repo-q (om/get-query RepoItem)
               stack-q (om/get-query StackItem)
               component-q (om/get-query ComponentItem)]
           `[{:users ~user-q}
             {:repos ~repo-q}
             {:stacks ~stack-q}
             {:components ~component-q}]))
  Object
  (render [this]
          (let [{:keys [users repos stacks components]} (om/props this)]
            (html [:div
                   [:nav.navbar.navbar-inverse.navbar-fixed-top
                    [:a.navbar-brand "Hecatoncheires"]]
                   [:br]
                   [:div.container
                    [:div.row
                     [:div.col-md-11
                      [:h1 "Components"]]
                     [:div.col-md-1
                      [:button.btn.btn-default.btn-sm.pull-right.border-0.fa.fa-plus
                       {:type "button"
                        :data-toggle "modal"
                        :data-target (str "#" "component-form-modal")}]]]
                    [:br]
                    (component-list components)]
                   (component-form-modal {:users users
                                          :repos repos
                                          :stacks stacks
                                          :modal-id "component-form-modal"})]))))
