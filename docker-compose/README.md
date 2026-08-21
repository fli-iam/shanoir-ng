
## Optimisations it the dockerfiles

We use multiple strategies to decrease the time to build the docker images and
the overall size of the final image:

- **common base image**: all images requiring a JRE begin with identical steps
  (delimited with `-- common jre base image --` in the dockerfiles), so as to
  be built only once. Extra packages are installed using an additional RUN
  step.

- [**multi-stage dockerfiles**][multi-stage] are used for:
    - running time-consuming steps in parallel (eg: in *nifti-conversion* we
      have a separate stage for installing dicomifier and building dcm2niix)
    - reducing the size of the final image:
      - by not installing packages that are only needed at build time (eg: gcc,
        curl, ...)
      - by not storing intermediate files (eg: dcm4che zip archive in
        *datasets*)

- **reduced number of steps**: prefer a single RUN step with all commands
  in a shell script rather than a series short RUN steps

- **caching**: incremental build using the buildkit cache

  - use [**RUN mount --type=cache**][mount-cache] to store the cache of package
    managers (eg: apt, conda) in external volumes, which yields two benefits:
    - reduced build time (no need to re-download the packages in future builds)
    - reduced image size (the package cache is not stored in the final image)

  - use [**COPY --link**][copy-link] as much as possible to make the step
    independent from the previous ones, which yields two benefits:
    - it does not need to be rebuilt when the previous step have changed
    - it can be build without extracting the image of the previous steps, this
      is significant for large images (eg. the extraction of *nifti-conversion*
      takes around 50 seconds)


[multi-stage]: https://docs.docker.com/build/building/multi-stage/
[copy-link]: https://docs.docker.com/reference/dockerfile/#copy---link
[mount-cache]: https://docs.docker.com/reference/dockerfile/#run---mounttypecache
