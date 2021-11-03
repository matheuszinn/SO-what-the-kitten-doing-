# What the kitten doing ?


> Concurrency and threads are cute as kittens  


Programa desenvolvido em clojure para a disciplina de Sistemas Operacionais. Consiste em um app que usa a API [The Cat API](https://thecatapi.com/) para obter imagens de gatos e informações sobre raças de gatinhos domésticos.
A ideia é spawnar duas Threads para buscar imagens novas e mostrar essas imagens na interface, fazendo isso a cada *x* segundos.

Enquanto isso, a Thread principal mostra no console informações sobre raças de gatinhos!!!

É mais Java que clojure se for analisar, mais o importante é que funciona.

## Modo de uso

Você pode rodar com: 

``lein run``, se tiver o leiningen instalado

ou baixando a [release](https://github.com/matheuszinn/SO-what-the-kitten-doing-/releases):

```
java -jar release_name
```

## License

Veja o arquivo `LICENSE`
