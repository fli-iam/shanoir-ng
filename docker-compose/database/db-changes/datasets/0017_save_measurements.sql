CREATE TABLE `measurement_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKpfn4cwjhfg9eur6k08m0n6duo` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
);
