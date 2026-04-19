## Creating your world

It is up to you to create your sections however you like, with whatever overlap you like.

#### DeeperWorld simply needs to know:
- X-Z coordinates of two corners of each section.
- A reference location on one section, and where the corresponding location is in the next.

## Configuring

Open The plugin config file under `plugins/DeeperWorld/config.yml`.

### What you'll be defining:

<img src="https://user-images.githubusercontent.com/16233018/97783275-025d8280-1b6d-11eb-9a68-d15aadc1b50d.png" width="600px"></img>

As you can see in this masterpiece of an illustration, DeeperWorld automatically knows the overlap to synchronize just by defining two locations.

There is also no restriction on differently sized regions. DeeperWorld will prevent teleports between two sections (red area) by bouncing players up/down if they would otherwise result in teleporting out of bounds.

### Example config

The plugin comes with an example config similar to the following:

```yaml
sections:
  - name: section1 #(1)
    refTop: 0, 0, 0
    refBottom: 0, 16, 0 #(2)
    region:
      start: 0,0,0
      end: 1000,256,1000 #(3)
    world: world
  - name: section2
    refTop: 1000, 240, 0 #(4)
    refBottom: 2000, 16, 0 #(5)
    region:
      start: 1000,0,0
      end: 2000,256,1000 1000
    world: world
```

1. Will show up when using /dw linfo
2. A location in this section that corresponds to the lower section's `refTop`
3. Corners for this section (x1, z1, x2, z2)
4. A location in this section that corresponds to the higher section's `refBottom`
5. Since this is the last section, the bottom won't matter
