# NeXAS_DX

A JavaFX GUI for converting **NeXAS engine** game data (GIGA/戯画) **to and from JSON files**.

---

## Supported Formats

Only tested on **`baldr_sky_divex`**,  
but `.spm` and `.bin` should work for most games of NeXAS engine since they use the same formats.

- **Parse to JSON**
  ```
  .waz  .mek  .spm  .grp  .bin
  ```

- **Generate from JSON**
  ```
  .waz  .mek  .spm  .grp  .bin
  ```

---

## Notes

- **`.grp` parsing** depends on the file name. Make sure the name is one of the following:
  ```
  BatVoice.grp
  MekaGroup.grp
  SeGroup.grp
  SpriteGroup.grp
  Term.grp
  WazaGroup.grp
  ```

- **`.mek` analysis** is incomplete.

---

## Tool Sources

- Unpack / pack: [NexasPackEdit](https://github.com/pkuislm/NexasPackEdit)

---

## Environment

- Java 17 (full):  
  [BellSoft JDK 17.0.15+10 (Windows, x64, Full)](https://download.bell-sw.com/java/17.0.15+10/bellsoft-jdk17.0.15+10-windows-amd64-full.msi)

---

## Purpose

The final purpose is to **transplant the `baldr_heart_exe` characters into `baldr_sky_divex`**.

---

## How to Use

*(not yet)*