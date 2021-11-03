;; A ideia da aplicação é gerar uma tela com duas imagens de gato, que são aleatorizadas por duas threads
;; A main thread cuida da renderização da GUi e de mostrar fatos felinos no terminal


;; Importa as libs
(ns what-the-kitty-doing-lein.core
  (:import (javax.swing JFrame JPanel JLabel ImageIcon SwingUtilities JProgressBar)
           (java.awt Image FlowLayout Container Component Dimension)
           (java.net URL)
           (javax.imageio ImageIO)
           (sun.awt.image ToolkitImage))
  (:require [clj-http.client :as client])
  (:require [clojure.data.json :as json])
  (:gen-class))


;; Átomo (referência de um valor) que guarda informações de 57 raças diferentes de gatos
;; Acessa uma api para obter essas informações
(def all-breeds (atom (-> "https://api.thecatapi.com/v1/breeds?api_key=72814159-0e54-42d1-8554-652ee1e14fc5"
                          client/get
                          :body
                          json/read-json)))

;; Retornam uma raça de gato aleatória
(defn random-breed
  []
  (get @all-breeds (rand-int (+ 1 (count @all-breeds)))))


;; Retorna uma string formatando as informações dessa raça
(defn breed-text
  []
  (let [breed (random-breed)]
    (format
      "\033[33;1mRaça: %s%nVive entre %s anos%nTemperamento: %s%nDescrição: %s%nLink da Wikipedia: %s%n" (:name breed) (:life_span breed) (:temperament breed) (:description breed) (:wikipedia_url breed))))


;; Faz acesso a uma API de imagem de gatos e retorna um objeto ImageIcon
(defn cat-imageIcon
  "Get the response from the API"
  []
  (-> "https://api.thecatapi.com/v1/images/search"
      client/get
      :body
      json/read-json
      first
      :url
      (URL.)
      (ImageIO/read)
      (.getScaledInstance 500 500 Image/SCALE_DEFAULT)
      (ImageIcon.)))


;; Retorna uma imagem aleatória de gato do tipo BufferedImage
(defn cat-image
  "Get the response from the API"
  []
  (.getBufferedImage ^ToolkitImage (.getImage (cat-imageIcon))))

;; Definindo os átomos que serão as referências para as imagens
;; Desse jeito, poderemos acessar o recurso em várias threads direfentes

(def l-img-atom (atom (cat-imageIcon)))
(def r-img-atom (atom (cat-imageIcon)))


;; Definindo os átomos dos JPanels que conterão as imagens

(def l-jpanel-atom (atom (JPanel. (FlowLayout.))))
(def r-jpanel-atom (atom (JPanel. (FlowLayout.))))

;; Define o main app, que implementa uma interface utilizando a lib java Swing

(defn kitten-app
  []
  (let [frame (doto (JFrame. "Kittens")
                (.setLocationRelativeTo nil)
                (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE))
        l-img-panel (deref l-jpanel-atom) ;; deref chama o valor do átomo
        r-img-panel (deref r-jpanel-atom)
        content (doto (.getContentPane frame)
                  (.setLayout (FlowLayout.)))
        l-img (deref l-img-atom)
        r-img (deref r-img-atom)
        progress (JProgressBar.)] ;; Progress bar infinita com o intuito de mostrar que a thread principal não é bloqueada quando a imagem muda

    (.add ^Container l-img-panel ^Component (JLabel. ^ImageIcon l-img))
    (.add ^Container r-img-panel ^Component (JLabel. ^ImageIcon r-img))

    (doto progress
      (.setIndeterminate true)
      (.setPreferredSize (Dimension. 1000 30)))

    (doto content
      (.add ^Container l-img-panel)
      (.add ^Container r-img-panel)
      (.add ^Container progress))

    (doto frame
      (.pack)
      (.setSize 1100 600)
      (.setVisible true))))


;; Atualiza o painel que contém a imagem da direita, após a troca de imagem ter sido feita
(defn update-right-image
  []
  (SwingUtilities/invokeLater #(doto (deref r-jpanel-atom)
                                 (.repaint))))

;; Atualiza o painel que contém a imagem da esquerda, após a troca de imagem ter sido feita
(defn update-left-image
  []
  (SwingUtilities/invokeLater #(doto (deref l-jpanel-atom)
                                 (.repaint))))


;;Define a função principal
(defn -main [& args]
  (println "Iniciando a Main Thread (Thread da GUI)")
  (SwingUtilities/invokeLater kitten-app)  ;;Chama a função kitten, que inicia a GUI

  (println "Iniciando a Thread 1 (imagem da direita")
  ;;Inicia thread 1 que muda a imagem da direita a cada 7,4 segundos
  (doto
    (Thread. ;;Cria o objeto da Thread
      ^Runnable
      (fn []                                                                    ;;Cria uma função anônima para rodar dentro da Thread
        (let [cat-future (future (cat-image))];;(future) gera uma objeto que vai avaliar a função (cat-image) em outra thread
          (Thread/sleep 7468) ;;Pausa a Thread

          (println "\033[31;1m[Thread 1]\033[1m- Mudando imagem da direita!") ;;Avisa que a imagem mudou
          (swap! r-img-atom (;;(swap!) muda o valor do átomo passado baseado no retorno de uma função
                              fn [i] (doto i ;;Essa função retona o novo valor do átomo
                                       (.setImage @cat-future)))) ;;@cat-future representa o valor da função (cat-image) que estava sendo avaliada em outra Thread
                                                                  ;;caso ela ainda esteja processando, a chamada @cat-future vai travar essa thread até que se tenha uma valor retornado
          (update-right-image));;Atualiza o painel com a nova imagem
        (recur))) ;;Chama esse bloco de código infinitamente
    (.start)) ;; Inicia essa thread

  ;; Aqui acontece a mesma coisa já explicada na Thread 1
  (println "Iniciando Thread 2 (imagem da esquerda)")
  ;; Inicia thread 2 que muda a imagem da esquerda a cada 10.3 segundos
  (doto
    (Thread.
      ^Runnable
      (fn []
        (let [cat-future (future (cat-image))]
          (Thread/sleep 13548)

          (println "\033[32;1m[Thread 2]\033[1m- Mudando imagem da direita!")
          (swap! l-img-atom (fn [i] (doto i
                                      (.setImage @cat-future))))
          (update-left-image))
        (recur)))
    (.start))



  (println "\033[33;1m[Main Thread] Começando a buscar informação de raça de gatos...")
  (loop [] ;;Criado um loop infinito que vai ficar mostrando as informações das raças dos gatos
    (Thread/sleep 5046) ;; Pausa a thread
    (println (breed-text)) ;; Busca as informações das raças e printa no console
    (recur))) ;; Chama tudo denovo, infinitamente...

(-main) ; Invoca a função -main
